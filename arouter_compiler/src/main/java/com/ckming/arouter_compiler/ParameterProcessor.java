package com.ckming.arouter_compiler;

import com.ckming.arouter_annotation.Parameter;
import com.ckming.arouter_compiler.utils.ProcessorConfig;
import com.ckming.arouter_compiler.utils.ProcessorUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

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
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by ptm on 2021/4/23.
 * <p>
 * 路由传参处理器
 */
@AutoService(Processor.class)   //开启服务
@SupportedAnnotationTypes({ProcessorConfig.PARAMETER_PACKAGE})   //服务的注解
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ParameterProcessor extends AbstractProcessor {

    private Elements elementUtils; // 类信息
    private Types typeUtils;  // 具体类型
    private Messager messager; // 日志
    private Filer filer;  // 生成器

    //用于存放被 @Parameter注解的属性集合  临时map存储
    //<类节点，@..>
    private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {

        // 扫描看那些地方使用到了@Parameter注解
        if (!ProcessorUtils.isEmpty(set)) {
            // 获取所有被 @Parameter 注解的 元素（属性）集合
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Parameter.class);

            if (!ProcessorUtils.isEmpty(elements)) {
                // TODO: 2021/4/23  存储相关信息
                for (Element element : elements) {  //element == 属性名

                    //字段节点的上一个节点，即属性上的父节点：类节点 == Order_MainActivity
                    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                    if (tempParameterMap.containsKey(enclosingElement)) {
                        tempParameterMap.get(enclosingElement).add(element);
                    } else {
                        List<Element> fields = new ArrayList<>();
                        fields.add(element);
                        tempParameterMap.put(enclosingElement, fields); // 加入缓存中
                    }
                }

                // TODO: 2021/4/23 生成类文件
                if (ProcessorUtils.isEmpty(tempParameterMap)) return true;

                TypeElement activityType = elementUtils.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
                TypeElement parameterType = elementUtils.getTypeElement(ProcessorConfig.AROUTER_AIP_PARAMETER_GET);

                //生成方法
                ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, ProcessorConfig.PARAMETER_NAME).build();

                //key：Order_MAinActivity
                //value：[name,age]
                for (Map.Entry<TypeElement, List<Element>> entry : tempParameterMap.entrySet()) {
                    TypeElement typeElement = entry.getKey();

                    if (!typeUtils.isSubtype(typeElement.asType(), activityType.asType())) {
                        throw new RuntimeException("@Parameter注解仅限用于Activity之上");
                    }

                    ClassName className = ClassName.get(typeElement);

                    //生成方法

                    ParameterFactory factory = new ParameterFactory.Builder(parameterSpec)
                            .setMessager(messager)
                            .setClassName(className)
                            .build();


                    factory.addFirstStatement();

                    //多个传参属性
                    for (Element element : entry.getValue()) {
                        factory.buildStatement(element);
                    }

                    String finalClassName = typeElement.getSimpleName() + ProcessorConfig.PARAMETER_FILE_NAME;
                    messager.printMessage(Diagnostic.Kind.NOTE, "APT生成获取的参数类文件：" +
                            className.packageName() + "." + finalClassName);

                    try {
                        JavaFile.builder(className.packageName(),   //包名
                                TypeSpec.classBuilder(finalClassName)   //类名
                                        .addSuperinterface(ClassName.get(parameterType))  //implements ParameterGet 实现ParameterLoad接口
                                        .addModifiers(Modifier.PUBLIC)  //public修饰符
                                        .addMethod(factory.build()) //方法构建
                                        .build())
                                .build()
                                .writeTo(filer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return false;   //执行一次检查就行
    }
}
