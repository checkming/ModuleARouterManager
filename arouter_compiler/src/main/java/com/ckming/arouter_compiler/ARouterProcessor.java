package com.ckming.arouter_compiler;

import com.ckming.arouter_annotation.ARouter;
import com.ckming.arouter_annotation.bean.RouterBean;
import com.ckming.arouter_compiler.utils.ProcessorConfig;
import com.ckming.arouter_compiler.utils.ProcessorUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by ptm on 2021/4/21.
 * 路由处理器
 */

// AutoService则是固定的写法，加个注解即可
// 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，用来注册
// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor.class)

//允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({ProcessorConfig.AROUTER_PACKAGE})

//指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)

//注解处理器接收的参数
@SupportedOptions({ProcessorConfig.OPTIONS, ProcessorConfig.APT_PACKAGE})

public class ARouterProcessor extends AbstractProcessor {

    //操作Element的工具类（类 函数 属性 ）
    private Elements elementTool;

    //Message用来打印日志相关信息
    private Messager message;

    //文件生成器 类 资源等 即最终要生成的文件 需要用Filer来完成的
    private Filer filer;

    //type(类信息)的工具类，包含 用于操作TypeMirror的工具方法
    private Types typeTool;

    //待验证模块传递过来的模块名
    private String options;
    //模块传递过来待验证的目录，用于统一存放apt生成的文件
    private String aptPackage;

    // 仓库一 Path  缓存一
    // Map<"personal", List<RouterBean>>
    private Map<String, List<RouterBean>> mAllPathMap = new HashMap<>();

    // 仓库二 Group 缓存二
    // Map<"personal", "ARouter$$Path$$personal.class">
    private Map<String, String> mAllGroupMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementTool = processingEnvironment.getElementUtils();
        message = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        typeTool = processingEnvironment.getTypeUtils();

        //验证APT环境搭建
        options = processingEnvironment.getOptions().get(ProcessorConfig.OPTIONS);
        aptPackage = processingEnvironment.getOptions().get(ProcessorConfig.APT_PACKAGE);
        message.printMessage(Diagnostic.Kind.NOTE, "-------options：" + options + "----------" +
                "-aptPackage：" + aptPackage + "--------");
        if (options != null && aptPackage != null) {
            message.printMessage(Diagnostic.Kind.NOTE, "APT 环境搭建完成");
        } else {
            message.printMessage(Diagnostic.Kind.NOTE, "APT 环境搭建有问题，请检查。");
        }
    }

    /**
     * 此函数等同于main函数，开始处理注解
     * 注解处理器的核心方法 处理具体的注解，生成java文件
     *
     * @param set              使用了支持处理注解的节点集合
     * @param roundEnvironment 当前或是之前的运行环境，可以通过该对象查找的注解
     * @return true 表示后续处理器不会再处理
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            message.printMessage(Diagnostic.Kind.NOTE, "并未发现有比ARouter注解的地方");
            return false;   //没机会再处理
        }

        // 获取所有被 @ARouter注解的元素集合
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);

        //通过Element工具类 获取Activity Callback类型
        TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
        //显示类信息 (获取被注解的节点 类节点) 自描述 Mirror
        TypeMirror activityMirror = activityType.asType();

        for (Element element : elements) {
            //获取类节点 获取包节点 (com.ckming.xxx)
            // String packageName = elementTool.getPackageOf(element).getQualifiedName().toString();

            //获取简单类名
            String className = element.getSimpleName().toString();
            message.printMessage(Diagnostic.Kind.NOTE, "被@ARouter注解的类有："
                    + className);  //打印出来即证明APT环境没有问题

            //拿到注解
            ARouter aRouter = element.getAnnotation(ARouter.class);

            /**
             * package com.example.helloworld;
             *
             * public final class HelloWorld {
             *   public static void main(String[] args) {
             *     System.out.println("Hello, JavaPoet!");
             *   }
             * }
             */

            //练习 JavaPort
            //1.方法
            MethodSpec mainMethod = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(String[].class, "args")
                    .addStatement("$T.out.println($S)", System.class, "Hello,JavaPoet!")
                    .build();

            //2.类
            TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(mainMethod)
                    .build();

            //3.包
            JavaFile packageF = JavaFile.builder("com.ckming.arouter", helloWorld)
                    .build();
            try {
                packageF.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
                message.printMessage(Diagnostic.Kind.NOTE, "生成失败，请检查代码");
            }

            //-----------------------


            // TODO: 2021/4/21  一系列的检查工作
            //在循环里对 路由对象 进行封装
            RouterBean routerBean = new RouterBean.Builder()
                    .addGroup(aRouter.group())
                    .addPath(aRouter.path())
                    .addElement(element)
                    .build();

            //ARouter注解的类 必须继承Activity
            TypeMirror elementMirror = element.asType();  //是否继承了Activity
            if (typeTool.isSubtype(elementMirror, activityMirror)) {
                routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY);  //证明是Activity
            } else {
                //不匹配抛出异常
                throw new RuntimeException("@ARouter注解目前仅限用于Activity类之上");
            }


            //系列检查工作
            if (checkRouterPath(routerBean)) {
                message.printMessage(Diagnostic.Kind.NOTE, "RouterBean " +
                        "Check Success!" + routerBean.toString());

                //赋值'
                List<RouterBean> routerBeans = mAllPathMap.get(routerBean.getGroup());

                //如果从Map中找不到key为：bean.getGroup()的数据，就新建List集合再添加进Map
                if (ProcessorUtils.isEmpty(routerBeans)) {  //仓库一没有
                    routerBeans = new ArrayList<>();
                    routerBeans.add(routerBean);
                    mAllPathMap.put(routerBean.getGroup(), routerBeans);  //加入到仓库一种
                } else {
                    routerBeans.add(routerBean);
                }
            } else {
                message.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未" +
                        "按规范配置，如/app/MainActivity");
            }
        }

        //此时缓存一就存好了所有Path的值
        TypeElement pathType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_PATH);//ARouterPath的描述
        TypeElement groupType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_GROUP);//ARouterGroup的描述

        // TODO 第一大步: 生成系列的path
        try {
            createPathFile(pathType);
        } catch (IOException e) {
            e.printStackTrace();
            message.printMessage(Diagnostic.Kind.NOTE, "在生成PATH模板时，异常了 e:" + e.getMessage());
        }


        // TODO 第二大步: 生成Group组头
        try {
            createGroupFile(groupType, pathType);
        } catch (IOException e) {
            e.printStackTrace();
            message.printMessage(Diagnostic.Kind.NOTE, "在生成GROUP模板时，异常了 e:" + e.getMessage());
        }

        return true;  //坑！！  表示处理@ARouter注解正式完成！
    }


    /**
     * 生成路由组Group文件，如ARouter$$Group$$app
     *
     * @param groupType ARouterLoadGroup 接口信息
     * @param pathType  ARouterLoadPath接口信息
     */
    private void createGroupFile(TypeElement groupType, TypeElement pathType) throws IOException {
        //仓库二 缓存二 判断是否有需要生成的类文件
        if (ProcessorUtils.isEmpty(mAllGroupMap) || ProcessorUtils.isEmpty(mAllPathMap)) return;

        /**最终Group模版效果
         *
         public class ARouter$$Group$$order implements ARouterGroup {
        @Override public Map<String, Class<? extends ARouterPath>> getGroupMap () {
        Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
        groupMap.put("order", ARouter$$Path$$order.class);       // 寻找Path
        ...

        return groupMap;
        }
        }
         */

        //返回值  Map<String, Class<? extends ARouterPath>>
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),      //Map
                ClassName.get(String.class),   //Map<String,

                // TODO: 2021/4/22 Class<? extends ARouterPath>> 难度
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        // ? extends ARouterPath
                        WildcardTypeName.subtypeOf(ClassName.get(pathType)))
                // WildcardTypeName.supertypeOf() ? super
        );


        //方法 public Map<String, Class<? extends ARouterPath>> getGroupMap ()
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME)  //方法名
                .addAnnotation(Override.class)   //重写注解 @Override
                .addModifiers(Modifier.PUBLIC)   //public修饰符
                .returns(methodReturns);//方法返回值


        //对象容器 Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
        methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        // TODO: 2021/4/22 同上难度
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))),
                ProcessorConfig.GROUP_VAR1,
                ClassName.get(HashMap.class));

        //赋值  groupMap.put("order", ARouter$$Path$$order.class);
        for (Map.Entry<String, String> entry : mAllGroupMap.entrySet()) {
            methodBuilder.addStatement("$N.put($S, $T.class)",
                    ProcessorConfig.GROUP_VAR1,  //groupMap.put
                    entry.getKey(),  //order、app
                    ClassName.get(aptPackage, entry.getValue()));
        }


        // return groupMap;
        methodBuilder.addStatement("return $N", ProcessorConfig.GROUP_VAR1);

        //最终生成的类文件名 ARouter$$Group$$ + personal
        String finalClassName = ProcessorConfig.GROUP_FILE_NAME + options;

        message.printMessage(Diagnostic.Kind.NOTE, "APT生成路由组Group类文件：" +
                aptPackage + "." + finalClassName);

        //生成类文件：ARouter$$Group$$order
        JavaFile.builder(aptPackage,   //包名
                TypeSpec.classBuilder(finalClassName)   //类名
                        .addSuperinterface(ClassName.get(groupType))  //实现ARouterLoadGroup 接口 implement ARouterGroup
                        .addModifiers(Modifier.PUBLIC)  //public修饰符
                        .addMethod(methodBuilder.build()) //方法的构建 （方法参数 + 方法体）
                        .build())  //类构建完成
                .build()  //JavaFile构建完成
                .writeTo(filer);  //文件生成器开始生成类文件
    }

    /**
     * 系列Path的类 生成工作
     *
     * @param pathType ARouterPath 高层的标准
     */
    private void createPathFile(TypeElement pathType) throws IOException {
        //判断map仓库中，是否有需要生成的文件
        if (ProcessorUtils.isEmpty(mAllPathMap)) {
            return;  //连缓存一 仓库一 里面的值都没有 不用再继续连
        }

        /** 最终Path模版效果
         *
         public class ARouter$$Path$$order implements ARouterPath {
        @Override public Map<String, RouterBean> getPathMap () {
        Map<String, RouterBean> pathMap = new HashMap<>();
        pathMap.put("/order/Order_MainActivity",
        RouterBean.create(RouterBean.TypeEnum.ACTIVITY, Order_MainActivity.class,
        "/order/Order_MainActivity","order" ));
        ....

        return pathMap;
        }
        }
         */

        //倒序生成代码
        //Map<String,RouterBean>
        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),         // Map
                ClassName.get(String.class),      // Map<String,
                ClassName.get(RouterBean.class)   // Map<String,RouterBean>
        );

        //遍历仓库 app，order等
        for (Map.Entry<String, List<RouterBean>> entry : mAllPathMap.entrySet()) {
            //@Override public Map<String, RouterBean> getPathMap ()
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
                    .addAnnotation(Override.class)    //给方法添加注解  @Override
                    .addModifiers(Modifier.PUBLIC)    //public 修饰符
                    .returns(methodReturn);//把Map<String,RouterBean> 加入方法中返回


            //Map<String, RouterBean> pathMap = new HashMap<>();
            methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                    ClassName.get(Map.class),           //Map
                    ClassName.get(String.class),        //Map<String,
                    ClassName.get(RouterBean.class),    //Map<String,RouterBean>
                    ProcessorConfig.PATH_VAR1,          //Map<String,RouterBean> pathMap
                    ClassName.get(HashMap.class)
            );

            List<RouterBean> pathList = entry.getValue();
            for (RouterBean bean : pathList) {
                //pathMap.put("/order/Order_MainActivity",RouterBean.create(RouterBean.TypeEnum.ACTIVITY,
                // Order_MainActivity.class, "/order/Order_MainActivity","order" ));
                /**
                 $N == 变量 有引用
                 $L == TypeEnum.ACTIVITY
                 $S == 字符串
                 $T == 类、接口
                 */
                methodBuilder.addStatement("$N.put($S, $T.create($T.$L, $T.class, $S, $S))",
                        ProcessorConfig.PATH_VAR1,    //pathMap.put
                        bean.getPath(), //"/app/MainActivity"
                        ClassName.get(RouterBean.class),  //RouterBean
                        ClassName.get(RouterBean.TypeEnum.class), // RouterBean.Type
                        bean.getTypeEnum(), //枚举类型：Activity
                        ClassName.get((TypeElement) bean.getElement()), //Order_MainActivity.class
                        bean.getPath(), //"/order/Order_MainActivity"
                        bean.getGroup() //"order"
                );
            }

            //return pathMap;
            methodBuilder.addStatement("return $N", ProcessorConfig.PATH_VAR1);


            // TODO: 2021/4/22  因为有implements,所以方法和类要合为一体生成才行
            //public class ARouter$$Path$$order implements ARouterPath

            String finalClassName = ProcessorConfig.PATH_FILE_NAME + entry.getKey();
            message.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件：" +
                    aptPackage + "." + finalClassName);

            //生成类文件：ARouter$$Path$$order
            JavaFile.builder(aptPackage, //包名 APT存放的路径
                    TypeSpec.classBuilder(finalClassName) //类名
                            .addSuperinterface(ClassName.get(pathType)) //实现ARouterLoadPath接口  implements ARouterPath==pathType
                            .addModifiers(Modifier.PUBLIC) //public的修饰符
                            .addMethod(methodBuilder.build())  //方法的构建 （方法参数 + 方法体）
                            .build() //类构建完成
            ).build()  //JavaFile 构建完成
                    .writeTo(filer);  //文件生成器开始生成类文件


            //仓库二 缓存二 非常重要的一步：PATH路径生成后，需要赋值给路由组 mAllGroupMap
            mAllGroupMap.put(entry.getKey(), finalClassName);
        }
    }


    /**
     * 校验@ARouter注解的值 如果group未填写就从必填项的path中截取数据
     *
     * @param routerBean 路由详细信息 最终的实体封装类
     * @return 赋值成功
     */
    private boolean checkRouterPath(RouterBean routerBean) {
        String group = routerBean.getGroup();  //如"app"  "order"  "personal"
        String path = routerBean.getPath(); //  "/app/MainActivity"

        //1.校验
        //@ARouter注解中的path值，必须要以 / 开头 （模仿阿里ARouter规范）
        if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")) {
            message.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的" +
                    "path值，必须要以 / 开头 ");
            return false;
        }

        //eg: path = "/MainActivity" 最后一个 /  符号肯定在字符串的第一位
        if (path.lastIndexOf("/") == 0) {
            message.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按配置要求填写");
            return false;
        }

        //2.截取
        // 如：/app/MainActivity 截取出 app,order,personal 作为group
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        // app,order,personal == options

        //3.赋值
        if (!ProcessorUtils.isEmpty(group) && !group.equals(options)) {
            message.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group" +
                    "值必须和子模块名一致");
            return false;
        } else {
            routerBean.setGroup(finalGroup);
        }

        //赋值成功则没有问题
        return true;
    }
}
