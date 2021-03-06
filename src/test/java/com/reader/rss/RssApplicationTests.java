package com.reader.rss;


import com.reader.rss.mapper.ItemMapper;
import com.reader.rss.mapper.SiteMapper;
import com.reader.rss.mapper.UserMapper;
import com.reader.rss.pojo.Item;
import com.reader.rss.pojo.Site;
import com.reader.rss.pojo.User;
import com.reader.rss.service.io.IJsfile;
import com.reader.rss.service.redisservice.Iredisservice;
import com.reader.rss.service.resolveHtml.IRhtml;
import com.reader.rss.service.resolvexml.IStorageXml;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RssApplicationTests {
    @Autowired
    IRhtml a;
    @Autowired
    IJsfile jsfile;
    @Autowired
    Iredisservice iredisservice;
    @Autowired
    IStorageXml storageXml;
    @Test
    public void contextLoads() {
        System.out.println("===="+a.selfResolve("http://www.jianshu.com")+"========");
    }
    @Test
    public void testio()throws Exception{
//        jsfile.IO("https://www.csdn.net/");
    }
    @Autowired(required = false)
    UserMapper userMapper;
    @Autowired(required = false)
    SiteMapper siteMapper;
    @Test
    public void testxml() throws Exception{
//        jsfile.getXml("http://feeds2.feedburner.com/jandan");
//        jsfile.createXML("http://blog.sina.com.cn/rss/1284797513.xml");
//        System.out.println( jsfile.createXML("http://china232.libsyn.com/rss"));
//        jsfile.getXml("http://www.cnbeta.com/backend.php");
//        jsfile.reslovVersion();
//        jsfile.getXml("http://china232.libsyn.com/rss");
//        jsfile.p("http://www.ifanr.com/feed");
        jsfile.reslovHtml("http://www.ifanr.com/feed");
//        jsfile.reShowHtml("https://www.sohu.com");
//        System.out.println(usermapper.selectUserById("2"));
/*          Item item = new Item();
          item.setFavNum(100);
          itemMapper.insertSelective(item);*/
//        System.out.println(usermapper.selectByPrimaryKey("1"));
//        storageXml.updateRssSource("http://blog.sina.com.cn/rss/1284797513.xml",2);
//        System.out.println(iredisservice.getByKey("http://blog.sina.com.cn/rss/1284797513.xml",Item.class).toString());
        Site site=new Site();
        site.setSiteUrl("dsadsad");
        siteMapper.insert(site);

    }
}

