package com.ckming.arouter_compiler;

import com.ckming.arouter_annotation.Parameter;
import com.ckming.arouter_compiler.utils.ProcessorConfig;
import com.ckming.arouter_compiler.utils.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by ptm on 2021/4/23.
 *
 * 路由传参处理器
 */
public class ParameterProcessor extends AbstractProcessor {

    //类信息
    private Elements elementUtils;
    //具体类型
    private Types typeUtils;
    //日志
    private Messager message;
    //生成器
    private Filer filer;

    //用于存放被 @Parameter注解的属性集合  临时map存储
    //<类节点，@..>
    private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        message = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {

        if (ProcessorUtils.isEmpty(set)){
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Parameter.class);

            if (!ProcessorUtils.isEmpty(elements)){
                // TODO: 2021/4/23  存储相关信息
                for (Element element:elements){  //element == 属性名

                    //字段节点的上一个节点，即属性上的父节点：类节点 == Order_MainActivity
                    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                    if (tempParameterMap.containsKey(enclosingElement)){
                        tempParameterMap.get(enclosingElement).add(element);
                    }else {
                        ArrayList<Element> fields = new ArrayList<>();
                        fields.add(element);
                        tempParameterMap.put(enclosingElement,fields); // 加入缓存中
                    }
                }

                // TODO: 2021/4/23 生成类文件
                if (ProcessorUtils.isEmpty(tempParameterMap))return true;

                TypeElement activityType = elementUtils.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
                TypeElement parameterType = elementUtils.getTypeElement(ProcessorConfig.AROUTER_AIP_PARAMETER_GET);

                //生成方法
                ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, ProcessorConfig.PARAMETER_NAME).build();

                //key：Order_MAinActivity
                //value：[name,age]
                for (Map.Entry<TypeElement,List<Element>> entry:tempParameterMap.entrySet()){
                    TypeElement typeElement = entry.getKey();

                    if (!typeUtils.isSubtype(typeElement.asType(),activityType.asType())){
                        throw new RuntimeException("@Parameter注解仅限用于Activity之上");
                    }

                    ClassName className = ClassName.get(typeElement);
                }
            }
        }

        return false;
    }
}
