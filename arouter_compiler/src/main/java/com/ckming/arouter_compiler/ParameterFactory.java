package com.ckming.arouter_compiler;

import com.ckming.arouter_annotation.Parameter;
import com.ckming.arouter_compiler.utils.ProcessorConfig;
import com.ckming.arouter_compiler.utils.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Created by ptm on 2021/4/25.
 * 生成类参数获取 模板代码
 *
 * @Override public void getParameter(Object targetParameter) {
 * Personal_MainActivity t = (Personal_MainActivity) targetParameter;
 * t.name = t.getIntent().getStringExtra("name");
 * t.sex = t.getIntent().getStringExtra("sex");
 * }
 */
public class ParameterFactory {

    //方法的构建
    private MethodSpec.Builder mBuilder;

    //类名
    private ClassName mClassName;

    //Messager消息通知
    private Messager mMessager;

    private ParameterFactory(Builder builder) {
        this.mMessager = builder.mMessager;
        this.mClassName = builder.mClassName;

        mBuilder = MethodSpec.methodBuilder(ProcessorConfig.PARAMETER_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(builder.mParameterSpec);
    }

    //Personal_MainActivity t = (Personal_MainActivity) targetParameter;
    public void addFirstStatement() {
        mBuilder.addStatement("$T t = ($T) " + ProcessorConfig.PARAMETER_NAME,
                mClassName, mClassName);
    }

    public MethodSpec build() {
        return mBuilder.build();
    }


    //t.name = t.getIntent().getStringExtra("name");
    public void buildStatement(Element element) {
        //遍历注解的属性节点  生成函数体
        TypeMirror typeMirror = element.asType();

        //获取TypeKind 枚举类型的序列号
        int ordinalType = typeMirror.getKind().ordinal();

        //获取参数属性名 eg:name
        String fieldName = element.getSimpleName().toString();

        //获取注解的值
        String annotationValue = element.getAnnotation(Parameter.class).name();

        //解决注解的值在为空时的处理 （若有值就用默认属性的注解值）
        annotationValue = ProcessorUtils.isEmpty(annotationValue) ? fieldName : annotationValue;

        String finalValue = "t." + fieldName;

        String methodContent = finalValue + " = t.getIntent().";

        // TODO: 2021/4/25 枚举类型
        if (ordinalType == TypeKind.INT.ordinal()) {
            methodContent += "getIntExtra($S," + finalValue + ")";  //有默认值
        } else if (ordinalType == TypeKind.BOOLEAN.ordinal()) {
            methodContent += "getBooleanExtra($S," + finalValue + ")"; //有默认值
        } else {
            //String
            if (typeMirror.toString().equalsIgnoreCase(ProcessorConfig.STRING)) {
                methodContent += "getStringExtra($S)"; //没有默认值
            }
        }

        if (methodContent.endsWith(")")) {
            mBuilder.addStatement(methodContent, annotationValue);
        } else {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "目前仅暂时支持" +
                    "String、int、boolean类型传参");
        }
    }

    /**
     * 构建者设计模式
     */
    public static class Builder {

        //错误提示信息
        private Messager mMessager;

        private ClassName mClassName;

        private ParameterSpec mParameterSpec;

        public Builder(ParameterSpec spec) {
            this.mParameterSpec = spec;
        }

        public Builder setMessager(Messager message) {
            mMessager = message;
            return this;
        }

        public Builder setClassName(ClassName className) {
            mClassName = className;
            return this;
        }

        public ParameterFactory build() {
            if (mParameterSpec == null) {
                throw new IllegalArgumentException("parameterSpec方法参数体为空");
            }

            if (mClassName == null) {
                throw new IllegalArgumentException("方法内容中的className为空");
            }

            if (mMessager == null) {
                throw new IllegalArgumentException("messager为空，Messager用来报告错误、警告和其他提示信息");
            }

            return new ParameterFactory(this);
        }
    }
}

