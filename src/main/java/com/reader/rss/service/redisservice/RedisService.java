package com.reader.rss.service.redisservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.reader.rss.mapper.ItemMapper;
import com.reader.rss.pojo.Item;
import com.reader.rss.pojo.Site;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService implements Iredisservice {
    private static final int  expire = 24*60*60;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired(required = false)
    private ItemMapper itemMapper;
    @Override
    public void removeByKey(String key) {
        if(redisTemplate.hasKey(key)){
            redisTemplate.delete(key);
        }
    }


    @Override
    public <T> T getByKey(String mapkey,String key,Class<T> tClass) {
        String res = (String) redisTemplate.opsForHash().get(mapkey,key);
        if(res != null){
            return Jutil.convertString2Obj(res,tClass);
        }
        return null;
    }

    @Override
    public <T> List<T> getSiteItem(String mapkey) {
        return (List)redisTemplate.opsForHash().entries(mapkey);
    }

    @Override
    public void setValue(String key, Item value,long time_s) {
        String string = Jutil.convertObj2String(value);
//        redisTemplate.opsForValue().set(key,Jutil.convertObj2String(value),time_s, TimeUnit.SECONDS);
        redisTemplate.opsForHash().put("map"+value.getSiteId(),key,Jutil.convertObj2String(value));
    }

    @Override
    public void setValue(String key, String value) {
        redisTemplate.opsForValue().set(key,value);
    }

    @Override
    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key,Jutil.convertObj2String(value));
    }

    @Override
    public boolean isExists(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public void updateValue(List<Item> list,Site site) {
        for (Item item : list) {
            System.out.println(site.getSiteId()+"ffffffffffffffffffffffffffffffffffffffffff");
            if(!(redisTemplate.opsForHash().hasKey("map"+site.getSiteId(),item.getItemUrl()))) {//缓存中不存在该条目
                item.setSiteId(site.getSiteId());
                //差图片
                item.setItemDate(new Date());
                System.out.println(item);
                itemMapper.insert(item);//写数据库
            }

        }
        redisTemplate.opsForHash().delete("map"+list.get(0).getSiteId(),redisTemplate.opsForHash().keys("map"+list.get(0).getSiteId()));
        for(Item item:list){
            String str = Jutil.convertObj2String(item);
            Item item1 = Jutil.convertString2Obj(str,Item.class);
            item = itemMapper.selectNewItem();
            setValue(item.getItemUrl(),item,expire);//写缓存
        }
//        redisTemplate.expire("map"+list.get(0).getSiteId(),expire,TimeUnit.SECONDS);
    }

    @Override
    public void updateAttrubite(Item item) {
        itemMapper.updateByPrimaryKey(item);
        setValue(item.getItemUrl(),item,expire);
    }
}
