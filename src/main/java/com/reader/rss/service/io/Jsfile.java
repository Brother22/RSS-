package com.reader.rss.service.io;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.reader.rss.pojo.Content;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.*;
import com.sun.syndication.io.SyndFeedInput;
import org.apache.commons.io.input.BOMInputStream;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;
import com.rometools.rome.io.XmlReader;
import org.xml.sax.InputSource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.min;

@Service
public class Jsfile implements IJsfile {
    private final  static String jspath = "<script type=\"text/javascript\" src=\"attack.js\"></script>\r\n";
    private final  static String[] charcter = {".*</title>","<link>.*</link>","<description>.*</description>","<pubDate>.*</pubDate>"};
    private final  static String str = "<[^>]+>";
    private final  static String title_icon = "http[s]{0,1}://[^/]+";
    private final  static String hreix = "/favicon.";
    private final  static String[] origin_charcter = {"&lt;","&gt;","&amp;","&quot;","&nbsp;","&apos;","\n"," ","<!--.*-->","<!\\[CDATA\\[","]]>"};
    private final  static String[] new_charcter = {"<",">","&","\"","","'",""," ","","",""};
    @Autowired
    WebDriver driver;
    @Override
    public void reShowHtml(String url) throws Exception{
        System.setProperty("webdriver.chrome.driver","chromedriver.exe");
        WebDriver driver1 =  new ChromeDriver(new ChromeOptions().addArguments("--headless","--disable-gpu"));
        int start = url.indexOf("www");
        int end = url.indexOf("\\",start)-1;
        if(end == -2)
            end = url.length();
        String fileName = url.substring(start,end);
        File file = new File("src\\main\\resources\\"+fileName+"_temp.html");
        if(file.exists())
            file.delete();
        RandomAccessFile temp = new RandomAccessFile("src\\main\\resources\\"+fileName+"_temp.html","rw");
            driver1.get(url);
            for (int i = 0; i < 5; i++) {
                ((JavascriptExecutor) driver1).executeScript("window.scrollTo(0, document.body.scrollHeight)");
            }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        StringBuilder html = new StringBuilder(driver1.getPageSource());
        int pos = html.indexOf("</head>");
        html.insert(pos,jspath);
        String content  = new String((""+html).getBytes("utf-8"),"utf-8");
        temp.write(content.getBytes());
        temp.close();
        }

    @Override
    public List<Content> getXml(String content) throws Exception{
        InputStream inputStream = new ByteArrayInputStream(content.getBytes("utf-8"));
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = null;
        feed = input.build(new InputSource(inputStream));
        List entries = feed.getEntries();// 得到所有的标题<title></title>
        List<Content> list = new ArrayList<Content>();
        String desc = "";
        String date = "";
        for(int i = 1;i < entries.size();++i){
            SyndEntry entry = (SyndEntry) entries.get(i);
            if(entry.getDescription() != null)
                desc = entry.getDescription().getValue();
            if(entry.getUpdatedDate() != null)
                date = entry.getUpdatedDate().toString();
            list.add(new Content(entry.getTitle(),entry.getLink(),desc,"","",""));
        }
        return list;
    }

    @Override
    public List<Content> createXML(String url) throws Exception{
        String html = "";
        synchronized (driver) {
            driver.get(url);
            html = driver.getPageSource();
        }
            int start = html.indexOf("<rss");
                if (start == -1) {
                    start = html.indexOf("&lt;rss");
                }
           int end = html.indexOf("</rss>");
            if(end == -1){
                end = html.indexOf("&lt;/rss&gt;")+6;
            }
           String content = html.substring(start,end+6);
            trimPage(content);
         return getXml(content);
    }
    @Override
    public List<Content> reslovVersion(String content){
        int t_s=0;
        int t_e=0;
        int u_s=0;
        int u_e=0;
        int d_s=0;
        int d_e=0;
        int date_s=0;
        int date_e=0;
        char[] ch = content.toCharArray();
        String title="";
        String url = "";
        String descr = "";
        String date="";
        List<Content> list = new ArrayList<Content>();
        String[] strs = content.split("&lt;title&gt;");
        for(int i = 2;i < strs.length;++i){
            t_s = 0;
            t_e = strs[i].indexOf("&lt;/title&gt;");
            title+=strs[i].substring(t_s,t_e);

            u_s = strs[i].indexOf("&lt;link&gt;")+12;
            u_e = strs[i].indexOf("&lt;/link&gt;",u_s);
            if(u_s != 11 && u_e != -1) {
                url += strs[i].substring(u_s, u_e);
            }

            d_s = strs[i].indexOf("&lt;description&gt;&lt;![CDATA[")+31;
            d_e = min(strs[i].indexOf("]]&gt;",d_s),strs[i].indexOf("&lt;",d_s));
            if(d_e != -1 && d_s != -1) {
                descr += strs[i].substring(d_s, d_e);
            }

            date_s = strs[i].indexOf("&lt;pubDate&gt;")+15;
            date_e = min(strs[i].indexOf("&lt;/pubDate&gt;",date_s),strs[i].indexOf("&lt;",date_s));
            if(date_e != -1 && date_s != -1) {
                date += strs[i].substring(date_s, date_e);
            }
//            String title, String url, String descr, String img, String date, String icon
            list.add(new Content(title,url,descr,"","",""));
            title="";
            url="";
            descr="";
            date="";
            u_s=0;
            d_s=0;
            date_s=0;
        }
        return list;
    }
    @Override
    public void p(String str){
        String string3 = "<[^>]+>";
        String url_patter = "";
        driver.get(str);
//        Pattern url = Pattern.compile("(&lt;link&gt;|<link>|<link />|&lt;link /&gt;){1}(<!\[CDATA\[){0,1}[^]<>;\"]*");
//        Matcher matcher = url.matcher(driver.getPageSource());
//        Pattern url2 = Pattern.compile("(http){1}.*");
        Pattern p = Pattern.compile(string3);
        String content = driver.getPageSource();

        Pattern desc = Pattern.compile("(<description>|<description />){1}.*(</description>){1}");
//        content = trimPage(content);
//        System.out.println(content);
        Matcher matcher = desc.matcher(content);
//        Pattern url2 = Pattern.compile("(http){1}.*");
        Matcher matcher2,matcher3;
        int i = 0;
        int pos;
        String string="";
        String string2="";
        List<String> list = new ArrayList<String>();
        while(matcher.find()){
            string = matcher.group();
//            System.out.println(string+"\n");
            matcher3 = p.matcher(string);
//            System.out.println(string);
//            System.out.println(matcher3.replaceAll("")+"+++++=+++++++");
/*            if((pos = string.indexOf("<![CDATA[")) != -1){
                string = string.substring(pos+9);
            }*/
//            if(string != "")
//            string = string.substring(13);
//            System.out.println(string);
        }
/*            while(matcher.find()){
                string2 = matcher.group();
//                System.out.println(string2);
                matcher2 = url2.matcher(string2);
                while (matcher2.find()) {
                    string = matcher2.group();
                    if ((pos = string.indexOf("&lt")) != -1) {
                        string = string.substring(0, pos);
                    }
                    list.add(string);
                    i++;
                }
        }
        System.out.println(list+" "+i);*/
    }

    @Override
    public String[] trimPage(String page) {
        Pattern pattern = null;
        String image_source = "<rss[^>]*>.*</rss[^>]*>";
    for(int i = 0;i < new_charcter.length;++i) {
        pattern = Pattern.compile(origin_charcter[i]);
        Matcher matcher = pattern.matcher(page);
        page = matcher.replaceAll(new_charcter[i]);
    }
//        System.out.println(page);
        page = page.split("<rss")[1];
        page = page.split("</rss>")[0];
        String[] strings = page.split("<title>");
        return strings;
    }

    @Override
    public List<Content> reslovHtml(String url) {
//        String url_icon = getTitleiconByUrl(url);
        String page = "";
        synchronized (driver) {
            driver.get(url);
            page = driver.getPageSource();
        }
//       System.out.println(url_icon);
/*        if(url_icon == null)
            url_icon = getTitleiconByPage(driver.getPageSource());*/
//        System.out.println(driver.getPageSource());
        String[] Items = trimPage(page);
        String[] res = new String[charcter.length+1];
        Pattern pattern = null;
        Matcher matcher = null;
        String res1 = "";
        List<Content> list = new ArrayList<Content>();
        for(int i = 1;i < Items.length;++i) {
            for(int j = 0;j < charcter.length;++j) {
                pattern = Pattern.compile(charcter[j]);
                matcher = pattern.matcher(Items[i]);
                if(matcher.find()) {
                    res1 = matcher.group();
                    if(j == 2){
                        res[charcter.length] = getPicture(res1);
                    }
                    pattern = Pattern.compile(str);
                    matcher = pattern.matcher(res1);
                    if(matcher.find())
                    res[j] = matcher.replaceAll("");
                    else res[j] = "";
                }
                else{
                    res[j] = "";
                }
            }
            list.add(new Content(res[0],res[1],res[2].substring(0,min(60,res[2].length())),res[4],"",""));
        }
        return list;
    }

    @Override
    public String getTitleiconByPage(String source) {
        String res = "";
        source = source.split("head>")[1];
        Pattern pattern = Pattern.compile("<link.*href.*((\\.ico)|(\\.png)|(\\.jpg)|(\\.svg))+.*>");
        Matcher matcher = pattern.matcher(source);
        if(matcher.find()){
            res = matcher.group();
            pattern = Pattern.compile("[www|//]{1}[^\"]+");
            matcher = pattern.matcher(res);
            if(matcher.find()) {
                res = matcher.group();
                if(res.charAt(0) == '/' && res.charAt(1) == '/')
                return res.substring(2);
                return res;
            }
        }
        return "/img/rssicon.jpg";
    }

    @Override
    public String getTitleiconByUrl(String Url) {
        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile(title_icon);
        matcher = pattern.matcher(Url);
            if(matcher.find()) {
                String a = matcher.group() + hreix + "ico";
//                String origin_url = driver.getCurrentUrl();
                String u = "";
                synchronized (driver) {
                    driver.get(a);
                    u = driver.getPageSource();
                }
//                driver.get(origin_url);
              pattern = Pattern.compile("<img[^>]+");
                matcher = pattern.matcher(u);
                if(matcher.find()) {
                    String str = matcher.group();
                    pattern = Pattern.compile("http[s]{0,1}://.*ico");
                    matcher = pattern.matcher(str);
                    if (matcher.find()) return matcher.group();
                }
            }
        return null;
    }

    @Override
    public String getPicture(String page) {
        Pattern pattern = Pattern.compile("<img.*>");
        Matcher matcher = pattern.matcher(page);
        if(matcher.find()){
            String res = matcher.group();
            pattern = Pattern.compile("http[^\"]+");
            matcher = pattern.matcher(res);
            if(matcher.find()){
                res = matcher.group();
//                System.out.println(res);
                return res;
            }
        }
        return "default.jpg";
    }

    @Override
    public String getIcon(String url) {
        String icon_url = getTitleiconByUrl(url);
        if(icon_url == null){
            synchronized (driver) {
                driver.get(url);
                icon_url = getTitleiconByPage(driver.getPageSource());
            }
        }
        return icon_url;
    }
}

