package com.jd;

import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;



import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ES_Demo02 {

    private TransportClient client;

    @Before
    public void init() throws UnknownHostException {
        //获取settings
        //配置es集群的名字
        Settings settings = Settings.builder().put("cluster.name", "myes").build();
        //获取客户端
        TransportAddress transportAddress = new TransportAddress(InetAddress.getByName("hadoop01"), 9300);

        TransportAddress transportAddress2 = new TransportAddress(InetAddress.getByName("hadoop02"), 9300);

        TransportAddress transportAddress3 = new TransportAddress(InetAddress.getByName("hadoop03"), 9300);
        //获取client客户端
        client = new PreBuiltTransportClient(settings)
                .addTransportAddress(transportAddress)
                .addTransportAddress(transportAddress2)
                .addTransportAddress(transportAddress3);

    }

    /**
     * 批量添加数据
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void addIndexDatas() throws IOException, ExecutionException, InterruptedException {

        /**
         * 创建索引
         * */
        client.admin().indices().prepareCreate("player").get();
        //构建json的数据格式，创建映射
        XContentBuilder mappingBuilder = jsonBuilder()
                .startObject()
                .startObject("player")
                .startObject("properties")
                .startObject("name").field("type","text").field("index", "true").field("fielddata","true").endObject()
                .startObject("age").field("type","integer").endObject()
                .startObject("salary").field("type","integer").endObject()
                .startObject("team").field("type","text").field("index", "true").field("fielddata","true").endObject()
                .startObject("position").field("type","text").field("index", "true").field("fielddata","true").endObject()
                .endObject()
                .endObject()
                .endObject();
        PutMappingRequest request = Requests.putMappingRequest("player")
                .type("player")
                .source(mappingBuilder);
        client.admin().indices().putMapping(request).get();


        //批量添加数据开始

        BulkRequestBuilder bulkRequest = client.prepareBulk();

// either use client#prepare, or use Requests# to directly build index/delete requests
        bulkRequest.add(client.prepareIndex("player", "player", "1")
                .setSource(jsonBuilder()
                        .startObject()
                        .field("name", "郭德纲")
                        .field("age", 33)
                        .field("salary",3000)
                        .field("team" , "cav")
                        .field("position" , "sf")
                        .endObject()
                )
        );
        bulkRequest.add(client.prepareIndex("player", "player", "2")
                .setSource(jsonBuilder()
                        .startObject()
                        .field("name", "于谦")
                        .field("age", 25)
                        .field("salary",2000)
                        .field("team" , "cav")
                        .field("position" , "pg")
                        .endObject()
                )
        );
        bulkRequest.add(client.prepareIndex("player", "player", "3")
                .setSource(jsonBuilder()
                        .startObject()
                        .field("name", "岳云鹏")
                        .field("age", 29)
                        .field("salary",1000)
                        .field("team" , "war")
                        .field("position" , "pg")
                        .endObject()
                )
        );

        bulkRequest.add(client.prepareIndex("player", "player", "4")
                .setSource(jsonBuilder()
                        .startObject()
                        .field("name", "爱因斯坦")
                        .field("age", 21)
                        .field("salary",300)
                        .field("team" , "tim")
                        .field("position" , "sg")
                        .endObject()
                )
        );

        bulkRequest.add(client.prepareIndex("player", "player", "5")
                .setSource(jsonBuilder()
                        .startObject()
                        .field("name", "张云雷")
                        .field("age", 26)
                        .field("salary",2000)
                        .field("team" , "war")
                        .field("position" , "pf")
                        .endObject()
                )
        );
        bulkRequest.add(client.prepareIndex("player", "player", "6")
                .setSource(jsonBuilder()
                        .startObject()
                        .field("name", "爱迪生")
                        .field("age", 40)
                        .field("salary",1000)
                        .field("team" , "tim")
                        .field("position" , "pf")
                        .endObject()
                )
        );
        bulkRequest.add(client.prepareIndex("player", "player", "7")
                .setSource(jsonBuilder()
                        .startObject()
                        .field("name", "牛顿")
                        .field("age", 21)
                        .field("salary",500)
                        .field("team" , "tim")
                        .field("position" , "c")
                        .endObject()
                )
        );

        bulkRequest.add(client.prepareIndex("player", "player", "8")
                .setSource(jsonBuilder()
                        .startObject()
                        .field("name", "特斯拉")
                        .field("age", 20)
                        .field("salary",500)
                        .field("team" , "tim")
                        .field("position" , "sf")
                        .endObject()
                )
        );
        BulkResponse bulkResponse = bulkRequest.get();
        client.close();
    }





    @Test
    public void groupAndCount() {
        //1：构建查询提交
        SearchRequestBuilder builder = client.prepareSearch("player").setTypes("player");
        //2：指定聚合条件
        TermsAggregationBuilder team = AggregationBuilders.terms("player_count").field("team");
        //3:将聚合条件放入查询条件中
        builder.addAggregation(team);
        //4:执行action，返回searchResponse
        SearchResponse searchResponse = builder.get();
        Aggregations aggregations = searchResponse.getAggregations();


        for (Aggregation aggregation : aggregations) {
            StringTerms stringTerms = (StringTerms) aggregation;
            List<StringTerms.Bucket> buckets = stringTerms.getBuckets();

            for (StringTerms.Bucket bucket : buckets) {
                System.out.println(bucket.getKey()+"---->"+bucket.getDocCount());
            }
        }
    }




    /**
     * 统计每个球队中每个位置的球员数量
     */
    @Test
    public void teamAndPosition(){
        SearchRequestBuilder builder = client.prepareSearch("player").setTypes("player");
        TermsAggregationBuilder team = AggregationBuilders.terms("player_count").field("team");
        TermsAggregationBuilder position = AggregationBuilders.terms("posititon_count").field("position");
        team.subAggregation(position);
        SearchResponse searchResponse = builder.addAggregation(team).addAggregation(position).get();
        Aggregations aggregations = searchResponse.getAggregations();
        for (Aggregation aggregation : aggregations) {
            // System.out.println(aggregation.toString());
            StringTerms stringTerms = (StringTerms) aggregation;
            List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
            for (StringTerms.Bucket bucket : buckets) {
                long docCount = bucket.getDocCount();
                Object key = bucket.getKey();
                System.out.println("当前队伍名称为" +  key + "该队伍下有"+docCount + "个球员");

                Aggregation posititon_count = bucket.getAggregations().get("posititon_count");
                if(null != posititon_count){
                    StringTerms positionTrem = (StringTerms) posititon_count;
                    List<StringTerms.Bucket> buckets1 = positionTrem.getBuckets();
                    for (StringTerms.Bucket bucket1 : buckets1) {
                        Object key1 = bucket1.getKey();
                        long docCount1 = bucket1.getDocCount();
                        System.out.println("     该队伍下面的位置为" +  key1+"该位置下有" +  docCount1 +"人");
                    }
                }
            }
        }
    }




}
