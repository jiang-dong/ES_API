package com.jd;

import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;

import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class ES_Demo {
    private TransportClient client;

    @Before
    public void init() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name", "myes").build();
        client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("hadoop01"),9300))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("hadoop02"),9300));
    }




//  第一种创建索引的方式
    //自己拼装json创建索引保存到myindex1索引库下面的article当中去
    @Test
    public void createIndex(){
        String json = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"travelying out Elasticsearch\"" +
                "}";
        IndexResponse indexResponse = client.prepareIndex("myindex1", "article", "1")
                .setSource(json, XContentType.JSON).get();
        client.close();
    }

    //第二种创建索引的方式
    //使用map创建索引
    @Test
    public void index2() throws Exception {
        HashMap<String, String> jsonMap = new HashMap<String, String>();
        jsonMap.put("name", "zhangsan");
        jsonMap.put("sex", "1");
        jsonMap.put("age", "18");
        jsonMap.put("address", "bj");
        IndexResponse indexResponse = client.prepareIndex("myindex1", "article", "2")
                .setSource(jsonMap)
                .get();
        client.close();
    }

    //第三种
    //XcontentBuilder实现创建索引

    @Test
    public void index3() throws IOException {
        IndexResponse indexResponse = client.prepareIndex("myindex1", "article", "3")
                .setSource(new XContentFactory().jsonBuilder()
                        .startObject()
                        .field("name", "lisi")
                        .field("age", "18")
                        .field("sex", "0")
                        .field("address", "bj")
                        .endObject())
                .get();
        client.close();

    }


    //第四种
    // 将对象转换为json格式字符串进行创建索引
    public class Person implements Serializable {
        private Integer id;
        private String name ;
        private Integer age;
        private Integer sex;
        private String address;
        private String phone;
        private String email;
        private  String say;


        public Person() {
        }

        public Person(Integer id, String name, Integer age, Integer sex, String address, String phone, String email,String say) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.sex = sex;
            this.address = address;
            this.phone = phone;
            this.email = email;
            this.say = say;
        }

        public String getSay() {
            return say;
        }

        public void setSay(String say) {
            this.say = say;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public Integer getSex() {
            return sex;
        }

        public void setSex(Integer sex) {
            this.sex = sex;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    /**
     * 将java对象转换为json格式字符串进行创建索引
     */
    @Test
    public void objToIndex(){
        Person person = new Person();
        person.setAge(18);
        person.setId(20);
        person.setName("张三丰");
        person.setAddress("武当山");
        person.setEmail("zhangsanfeng@163.com");
        person.setPhone("18588888888");
        person.setSex(1);
        String json = JSONObject.toJSONString(person);
        System.out.println(json);
        client.prepareIndex("myindex1","article","32").setSource(json,XContentType.JSON).get();
        client.close();
    }


    //批量创建索引
    /**
     * 批量创建索引
     * @throws IOException
     */
    @Test
    public void index4() throws IOException {
        BulkRequestBuilder bulk = client.prepareBulk();
        bulk.add(client.prepareIndex("myindex1", "article", "4")
                .setSource(new XContentFactory().jsonBuilder()
                        .startObject()
                        .field("name", "wangwu")
                        .field("age", "18")
                        .field("sex", "0")
                        .field("address", "bj")
                        .endObject()));
        bulk.add(client.prepareIndex("news", "article", "5")
                .setSource(new XContentFactory().jsonBuilder()
                        .startObject()
                        .field("name", "zhaoliu")
                        .field("age", "18")
                        .field("sex", "0")
                        .field("address", "bj")
                        .endObject()));
        BulkResponse bulkResponse = bulk.get();
        System.out.println(bulkResponse);
        client.close();
    }




    ///**
    // * 更新索引
    // * 根据数据id来进行更新索引
    // */
    @Test
    public void updateIndex(){
        Person guansheng = new Person(5, "宋江", 88, 0, "水泊梁山", "17666666666", "wusong@itcast.com","及时雨宋江");
        client.prepareUpdate().setIndex("myindex1").setType("article").setId("8")
                .setDoc(JSONObject.toJSONString(guansheng),XContentType.JSON)
                .get();
        client.close();
    }





    /**
     * 按照id进行删除数据
     */
    @Test
    public void deleteById(){
        DeleteResponse deleteResponse = client.prepareDelete("myindex1", "article", "8").get();
        client.close();
    }


    /**
     * 删除索引
     * 删除整个索引库
     */
    @Test
    public  void  deleteIndex(){
      //  DeleteIndexResponse indexsearch =client.admin().indices().prepareDelete("myindex1").execute().actionGet();
        client.close();
    }





//
    //
    //           查询索引


    //初始化一批数据到索引库中准备查询
    /**
     * 初始化一批数据到索引库当中去准备做查询使用
     * 注意这里初始化的时候，需要给我们的数据设置分词属性
     * @throws Exception
     */
    @Test
    public void createIndexBatch() throws Exception {
        Settings settings = Settings
                .builder()
                .put("cluster.name", "myes") //节点名称， 在es配置的时候设置
                //自动发现我们其他的es的服务器
                .put("client.transport.sniff", "true")
                .build();
        //创建客户端
        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("hadoop01"), 9300));//以本机作为节点
        //创建映射
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("properties")
                //      .startObject("m_id").field("type","keyword").endObject()
                .startObject("id").field("type", "integer").endObject()
                .startObject("name").field("type", "text").field("analyzer", "ik_max_word").endObject()
                .startObject("age").field("type", "integer").endObject()
                .startObject("sex").field("type", "text").field("analyzer", "ik_max_word").endObject()
                .startObject("address").field("type", "text").field("analyzer", "ik_max_word").endObject()
                .startObject("phone").field("type", "text").endObject()
                .startObject("email").field("type", "text").endObject()
                .startObject("say").field("type", "text").field("analyzer", "ik_max_word").endObject()
                .endObject()
                .endObject();
        //pois：索引名   cxyword：类型名（可以自己定义）
        PutMappingRequest putmap = Requests.putMappingRequest("indexsearch").type("mysearch").source(mapping);
        //创建索引
        client.admin().indices().prepareCreate("indexsearch").execute().actionGet();
        //为索引添加映射
        client.admin().indices().putMapping(putmap).actionGet();


        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        Person lujunyi = new Person(2, "玉麒麟卢俊义", 28, 1, "水泊梁山", "17666666666", "lujunyi@itcast.com","hello world今天天气还不错");
        Person wuyong = new Person(3, "智多星吴用", 45, 1, "水泊梁山", "17666666666", "wuyong@itcast.com","行走四方，抱打不平");
        Person gongsunsheng = new Person(4, "入云龙公孙胜", 30, 1, "水泊梁山", "17666666666", "gongsunsheng@itcast.com","走一个");
        Person guansheng = new Person(5, "大刀关胜", 42, 1, "水泊梁山", "17666666666", "wusong@itcast.com","我的大刀已经饥渴难耐");
        Person linchong = new Person(6, "豹子头林冲", 18, 1, "水泊梁山", "17666666666", "linchong@itcast.com","梁山好汉");
        Person qinming = new Person(7, "霹雳火秦明", 28, 1, "水泊梁山", "17666666666", "qinming@itcast.com","不太了解");
        Person huyanzhuo = new Person(8, "双鞭呼延灼", 25, 1, "水泊梁山", "17666666666", "huyanzhuo@itcast.com","不是很熟悉");
        Person huarong = new Person(9, "小李广花荣", 50, 1, "水泊梁山", "17666666666", "huarong@itcast.com","打酱油的");
        Person chaijin = new Person(10, "小旋风柴进", 32, 1, "水泊梁山", "17666666666", "chaijin@itcast.com","吓唬人的");
        Person zhisheng = new Person(13, "花和尚鲁智深", 15, 1, "水泊梁山", "17666666666", "luzhisheng@itcast.com","倒拔杨垂柳");
        Person wusong = new Person(14, "行者武松", 28, 1, "水泊梁山", "17666666666", "wusong@itcast.com","二营长。。。。。。");

        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "1")
                .setSource(JSONObject.toJSONString(lujunyi), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "2")
                .setSource(JSONObject.toJSONString(wuyong), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "3")
                .setSource(JSONObject.toJSONString(gongsunsheng), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "4")
                .setSource(JSONObject.toJSONString(guansheng), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "5")
                .setSource(JSONObject.toJSONString(linchong), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "6")
                .setSource(JSONObject.toJSONString(qinming), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "7")
                .setSource(JSONObject.toJSONString(huyanzhuo), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "8")
                .setSource(JSONObject.toJSONString(huarong), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "9")
                .setSource(JSONObject.toJSONString(chaijin), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "10")
                .setSource(JSONObject.toJSONString(zhisheng), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "11")
                .setSource(JSONObject.toJSONString(wusong), XContentType.JSON)
        );

        bulkRequestBuilder.get();
        client.close();

    }




    /**
     * 通过id来进行精确查询
     */
    @Test
    public void query1() {
        GetResponse documentFields = client.prepareGet("indexsearch", "mysearch", "11").get();
        String index = documentFields.getIndex();
        String type = documentFields.getType();
        String id = documentFields.getId();
        System.out.println(index);
        System.out.println(type);
        System.out.println(id);
        Map<String, Object> source = documentFields.getSource();
        for (String s : source.keySet()) {
            System.out.println(source.get(s));
        }
    }



    /**
     * 查询所有数据
     */
    @Test
    public void queryAll() {
        SearchResponse searchResponse = client
                .prepareSearch("indexsearch")
                .setTypes("mysearch")
                .setQuery(new MatchAllQueryBuilder())
                .get();
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            System.out.println(sourceAsString);
        }
        client.close();
    }


    /**RangeQuery范围值查询
     * 查找年龄18到28的人,包含18和28
     */
    @Test
    public void  rangeQuery(){
        SearchResponse searchResponse = client.prepareSearch("indexsearch")
                .setTypes("mysearch")
                .setQuery(new RangeQueryBuilder("age").gt(17).lt(29))
                .get();
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit documentFields : hits1) {
            System.out.println(documentFields.getSourceAsString());
        }
        client.close();
    }

    /**termQuery词条查询
     * 词条查询
     */
    @Test
    public  void termQuery(){
        SearchResponse searchResponse = client.prepareSearch("indexsearch").setTypes("mysearch")
                .setQuery(new TermQueryBuilder("say", "熟悉"))
                .get();
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit documentFields : hits1) {
            System.out.println(documentFields.getSourceAsString());
        }
    }

    /**fuzzyQuery模糊查询
     * fuzzyQuery表示英文单词的最大可纠正次数，最大可以自动纠正两次
     */
    @Test
    public void fuzzyQuery(){
        SearchResponse searchResponse = client.prepareSearch("indexsearch").setTypes("mysearch")
                .setQuery(QueryBuilders.fuzzyQuery("say", "helOL").fuzziness(Fuzziness.TWO))
                .get();
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit documentFields : hits1) {
            System.out.println(documentFields.getSourceAsString());
        }
        client.close();
    }


    /**
     * 模糊匹配查询有两种匹配符，分别是" * " 以及 " ? "， 用" * "来匹配任何字符，包括空字符串。用" ? "来匹配任意的单个字符
     */
    @Test
    public void wildCardQueryTest(){
        SearchResponse searchResponse = client.prepareSearch("indexsearch").setTypes("mysearch")
                .setQuery(QueryBuilders.wildcardQuery("say", "hel*"))
                .get();
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit documentFields : hits1) {
            System.out.println(documentFields.getSourceAsString());
        }
        client.close();
    }

    /**
     * 多条件组合查询 boolQuery
     * 查询年龄是18到28范围内且性别是男性的，或者id范围在10到13范围内的
     *
     */
    @Test
    public void boolQueryTest(){
        RangeQueryBuilder age = QueryBuilders.rangeQuery("age").gt(17).lt(29);
        TermQueryBuilder sex = QueryBuilders.termQuery("sex", "1");
        RangeQueryBuilder id = QueryBuilders.rangeQuery("id").gt("9").lt("15");

        SearchResponse searchResponse = client.prepareSearch("indexsearch").setTypes("mysearch")
                .setQuery(
                        QueryBuilders.boolQuery().should(id)
                                .should(QueryBuilders.boolQuery().must(sex).must(age)))
                .get();
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit documentFields : hits1) {
            System.out.println(documentFields.getSourceAsString());
        }
        client.close();
    }


    /*分页查询
     */
    @Test
    public void getPageIndex(){
        int  pageSize = 5;
        int pageNum = 2;
        int startNum = (pageNum-1)*5;
        SearchResponse searchResponse = client.prepareSearch("indexsearch")
                .setTypes("mysearch")
                .setQuery(QueryBuilders.matchAllQuery())
                .addSort("id",SortOrder.ASC)
                .setFrom(startNum)
                .setSize(pageSize)
                .get();
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit documentFields : hits1) {
            System.out.println(documentFields.getSourceAsString());
        }
        client.close();
    }


    /**
     * 高亮查询
     */
    @Test
    public  void  highLight(){
        //设置我们的查询高亮字段
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("indexsearch")
                .setTypes("mysearch")
                .setQuery(QueryBuilders.termQuery("say", "hello"));

        //设置我们字段高亮的前缀与后缀
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("say").preTags("<font style='color:red'>").postTags("</font>");

        //通过高亮来进行我们的数据查询
        SearchResponse searchResponse = searchRequestBuilder.highlighter(highlightBuilder).get();
        SearchHits hits = searchResponse.getHits();
        System.out.println("查询出来一共"+ hits.totalHits+"条数据");
        for (SearchHit hit : hits) {
            //打印没有高亮显示的数据
            System.out.println(hit.getSourceAsString());
            System.out.println("=========================");
            //打印我们经过高亮显示之后的数据
            Text[] says = hit.getHighlightFields().get("say").getFragments();
            for (Text say : says) {
                System.out.println(say);
            }

     /*   Map<String, HighlightField> highlightFields = hit.getHighlightFields();
        System.out.println(highlightFields);*/
        }
        client.close();
    }



    /**
     * 创建索引库
     * 创建mapping以及setting
     */

    public void createIndex01() {
        /**
         * Put document
         * {
         *      "settings": {
         *          "number_of_replicas":1,
         *          "number_of_shares": 5,
         *              "analysis": {
         *                "annalyzer":{
         *                     "ik":{
         *                        "tokenizer":"ik_max_word"
         *              }
         *              }
         *              },
         *      "mappings":{
         *              "doc":{
         *               "properties": {
         *                 "name":{
         *                   "type": "text",
         *                   "analyzer": "ik_max_word"
         *                 }
         *               }
         *             }
         *             }
         *
         *
         */

        CreateIndexRequestBuilder builder = client.admin().indices().prepareCreate("es_info");


    }
}


