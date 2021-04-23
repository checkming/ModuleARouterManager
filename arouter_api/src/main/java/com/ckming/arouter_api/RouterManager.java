package com.ckming.arouter_api;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.ckming.arouter_annotation.bean.RouterBean;

import androidx.annotation.RequiresApi;

/**
 * Created by ptm on 2021/4/22.
 * 路由跳转传参管理器
 */
public class RouterManager {
    private String group;  //路由的组名 如：app，order，personal等
    private String path;  // 路由的路径 如：/order/Order_MainActivity

    /**
     * 1.拿到 "ARouter$$Group$$ + 组名"后，根据组名 拿到 "ARouter$$Path$$+组名"
     * 2.再根据路径 拿到 类.class 就可以实现跳转了
     */
    public static RouterManager instance;

    public static RouterManager getInstance() {
        if (instance == null) {
            synchronized (RouterManager.class) {
                if (instance == null) {
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }

    //利用LruCache缓存 提高性能
    private LruCache<String, ARouterGroup> groupLruCache;
    private LruCache<String, ARouterPath> pathLruCache;

    //ARouter$$Group$$personal
    private final static String FILE_GROUP_NAME = "ARouter$$Group$$";

    private RouterManager() {
        groupLruCache = new LruCache<>(100);
        pathLruCache = new LruCache<>(100);
    }

    /**
     * eg：/order/Order_MainActivity
     *
     * @param path
     * @return
     */
    public BundleManager build(String path) throws IllegalAccessException {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new IllegalAccessException("path路径填错,准确的写法：/order/Order_MainActivity ");
        }

        if (path.lastIndexOf("/") == 0) {
            throw new IllegalAccessException("path路径填错,准确的写法：/order/Order_MainActivity ");
        }

        //等等各种健壮性判断

        //order，personal
        String finalGroup = path.substring(1, path.indexOf("/", 1));
        if (TextUtils.isEmpty(finalGroup)) {
            throw new IllegalAccessException("path路径填错,准确的写法：/order/Order_MainActivity ");
        }

        //此时的group path赋值正常且成功
        this.path = path;
        this.group = finalGroup;

        return new BundleManager();
    }


    //真正的导航
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public Object navigation(Context context, BundleManager bundleManager) {
        String groupClassName = context.getPackageName() + '.' + FILE_GROUP_NAME + group;
        Log.e("Ckming", "navigation：groupClassName" + groupClassName);

        try {
            // TODO: 2021/4/23 第一步 读取路由组Group类文件
            ARouterGroup loadGroup = groupLruCache.get(group);
            if (null == loadGroup) {
                //缓存没有则去 加载APT路由组Group类文件 如：ARouter$$Group$$order
                Class<?> aClass = Class.forName(groupClassName);

                //初始化类文件
                loadGroup = (ARouterGroup) aClass.newInstance();

                //保存到缓存
                groupLruCache.put(group, loadGroup);
            }

            if (loadGroup.getGroupMap().isEmpty()) {
                throw new RuntimeException("路由表Group加载失败");
            }

            // TODO: 2021/4/23  第二步 读取路由Path类文件
            ARouterPath loadPath = pathLruCache.get(path);
            if (null == loadPath) {
                Class<? extends ARouterPath> clazz = loadGroup.getGroupMap().get(group);

                //从map中获取
                loadPath = clazz.newInstance();

                //保存到缓存中
                pathLruCache.put(path, loadPath);
            }

            //跳转
            if (loadPath != null) {
                if (loadPath.getPathMap().isEmpty()) {
                    throw new RuntimeException("路由表Path报废了！");
                }

                RouterBean routerBean = loadPath.getPathMap().get(path);

                if (routerBean != null) {
                    if (routerBean.getTypeEnum() == RouterBean.TypeEnum.ACTIVITY) {
                        Intent intent = new Intent(context, routerBean.getMyClass());
                        intent.putExtras(bundleManager.getBundle());
                        context.startActivity(intent, bundleManager.getBundle());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    //原先的跳转方式
    /*ARouter$$Group$$order group$$order = new ARouter$$Group$$order();
    Map<String, Class<? extends ARouterPath>> groupMap = group$$order.getGroupMap();
    Class<? extends ARouterPath> myClass = groupMap.get("order");

        try {
        ARouter$$Path$$order path = (ARouter$$Path$$order) myClass.newInstance();
        Map<String, RouterBean> pathMap = path.getPathMap();
        RouterBean routerBean = pathMap.get("/order/Order_MainActivity");

        if (routerBean != null) {
            Intent intent = new Intent(this, routerBean.getMyClass());
            Log.e(Cons.TAG, "跳转成功！");
            startActivity(intent);
        }
    } catch (IllegalAccessException e) {
        e.printStackTrace();
    } catch (InstantiationException e) {
        e.printStackTrace();
    }*/
}
