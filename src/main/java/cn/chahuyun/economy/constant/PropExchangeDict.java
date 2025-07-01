package cn.chahuyun.economy.constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropExchangeDict {

    public static final Map<String, Map<String, Long>> PROP_EXCHANGE_COUNT = new HashMap<>(20);
    static {
//        FISH-101	🫧泡泡标识	兑换物品	道具兑换	消耗100个泡泡标识碎片获得，bubble bubble
        Map<String, Long> list101 = new HashMap<>();
        list101.putAll(bindCoutMap("FISH-111", 100L));
        PROP_EXCHANGE_COUNT.put("FISH-101", list101);
        //       FISH-102	🪙波币标识	兑换物品	道具兑换	消耗100个波币标识碎片获得，印有波波的代币
        Map<String, Long> list102 = new HashMap<>();
        list101.putAll(bindCoutMap("FISH-112", 100L));
        PROP_EXCHANGE_COUNT.put("FISH-102", list102);
//         FISH-103	🎰梭哈标识	兑换物品	道具兑换	消耗100个梭哈标识碎片获得，搏一搏！
        Map<String, Long> list103 = new HashMap<>();
        list103.putAll(bindCoutMap("FISH-113", 100L));
        PROP_EXCHANGE_COUNT.put("FISH-103", list103);

//        FISH-104	💴万贯标识	兑换物品	道具兑换	消耗10个万贯标识碎片获得，非常有钱
        Map<String, Long> list104 = new HashMap<>();
        list104.putAll(bindCoutMap("FISH-114", 10L));
        PROP_EXCHANGE_COUNT.put("FISH-104", list104);

//        FISH-105	准点打工标识	兑换物品	道具兑换	消耗60个准点打工标识碎片获得，希望你像准点上班一样准点来钓鱼
        Map<String, Long> list105 = new HashMap<>();
        list105.putAll(bindCoutMap("FISH-115", 60L));
        PROP_EXCHANGE_COUNT.put("FISH-105", list105);

//        FISH-106	动物大使标识	兑换物品	道具兑换	消耗60个动物大使标识碎片获得，智人才是地球之癌，所有的其它动物都不是
        Map<String, Long> list106 = new HashMap<>();
        list106.putAll(bindCoutMap("FISH-116", 60L));
        PROP_EXCHANGE_COUNT.put("FISH-106", list106);

//        FISH-107	狗姐心选标识	兑换物品	道具兑换	消耗80个狗姐心选标识碎片获得，姐姐看没看上你不知道，但是你被狗盯上了！
        Map<String, Long> list107 = new HashMap<>();
        list107.putAll(bindCoutMap("FISH-117", 80L));
        PROP_EXCHANGE_COUNT.put("FISH-107", list107);

//        FISH-108	天选之子标识	兑换物品	道具兑换	消耗100个天选之子标识碎片获得，就是你了！bobo的天选之子！
        Map<String, Long> list108 = new HashMap<>();
        list108.putAll(bindCoutMap("FISH-118", 80L));
        PROP_EXCHANGE_COUNT.put("FISH-108", list108);

    }

    public static Map<String, Long> bindCoutMap(String code, Long count){
        Map<String, Long> map = new HashMap<>(1);
        map.put(code, count);
        return map;
    }

    /**
     * 组成道具兑换
     */
    public static final Map<String, List<String>> PROP_EXCHANGE = new HashMap<>(20);
    static {
        // FBPFK
        List<String> bfpfkList = new ArrayList<>(5);

        bfpfkList.add("FISH-4");
        bfpfkList.add("FISH-3");
        bfpfkList.add("FISH-5");
        bfpfkList.add("FISH-3");
        bfpfkList.add("FISH-6");
        PROP_EXCHANGE.put("FISH-15",bfpfkList);
        // FBTNK
        List<String> bftnkList = new ArrayList<>(5);
        bftnkList.add("FISH-4");
        bftnkList.add("FISH-3");
        bftnkList.add("FISH-7");
        bftnkList.add("FISH-8");
        bftnkList.add("FISH-6");
        PROP_EXCHANGE.put("FISH-16",bftnkList);

        List<String> hkList = new ArrayList<>(6);
        hkList.add("FISH-9");
        hkList.add("FISH-10");
        hkList.add("FISH-11");
        hkList.add("FISH-12");
        hkList.add("FISH-13");
        hkList.add("FISH-24");
        PROP_EXCHANGE.put("FISH-17",hkList);

        List<String> storyList = new ArrayList<>(2);
        storyList.add("FISH-18");
        storyList.add("FISH-19");
        PROP_EXCHANGE.put("FISH-20",storyList);


        List<String> wditBBList = new ArrayList<>(4);
        wditBBList.add("FISH-35");
        wditBBList.add("FISH-36");
        wditBBList.add("FISH-37");
        wditBBList.add("FISH-38");
        PROP_EXCHANGE.put("FISH-39", wditBBList);

        List<String> physicalList = new ArrayList<>(6);
        physicalList.add("FISH-43");
        physicalList.add("FISH-44");
        physicalList.add("FISH-45");
        physicalList.add("FISH-46");
        physicalList.add("FISH-47");
        physicalList.add("FISH-48");
        PROP_EXCHANGE.put("FISH-49", physicalList);

        List<String> uranusList = new ArrayList<>(10);
        uranusList.add("FISH-52");
        uranusList.add("FISH-53");
        uranusList.add("FISH-54");
        uranusList.add("FISH-55");
        uranusList.add("FISH-56");
        uranusList.add("FISH-57");
        uranusList.add("FISH-58");
        uranusList.add("FISH-59");
        uranusList.add("FISH-60");
        uranusList.add("FISH-61");
        uranusList.add("FISH-63");
        uranusList.add("FISH-64");
        uranusList.add("FISH-65");
        uranusList.add("FISH-66");
        uranusList.add("FISH-67");
        uranusList.add("FISH-68");
        uranusList.add("FISH-69");
        uranusList.add("FISH-70");
        uranusList.add("FISH-71");
        uranusList.add("FISH-72");
        uranusList.add("FISH-73");
        uranusList.add("FISH-74");
        uranusList.add("FISH-75");
        uranusList.add("FISH-76");
        uranusList.add("FISH-77");
        uranusList.add("FISH-78");
        uranusList.add("FISH-79");
        uranusList.add("FISH-80");
        uranusList.add("FISH-81");
        uranusList.add("FISH-82");
        PROP_EXCHANGE.put("FISH-62", uranusList);



        List<String> FISH_95_LIST = new ArrayList<>(10);
        FISH_95_LIST.add("FISH-83");
        FISH_95_LIST.add("FISH-84");
        FISH_95_LIST.add("FISH-85");
        FISH_95_LIST.add("FISH-86");
        FISH_95_LIST.add("FISH-87");
        FISH_95_LIST.add("FISH-88");
        FISH_95_LIST.add("FISH-89");
        FISH_95_LIST.add("FISH-90");
        FISH_95_LIST.add("FISH-91");
        FISH_95_LIST.add("FISH-92");
        FISH_95_LIST.add("FISH-93");
        FISH_95_LIST.add("FISH-94");
        PROP_EXCHANGE.put("FISH-95", FISH_95_LIST);

        List<String> FISH_133_LIST = new ArrayList<>(7);
        FISH_95_LIST.add("FISH-126");
        FISH_95_LIST.add("FISH-127");
        FISH_95_LIST.add("FISH-128");
        FISH_95_LIST.add("FISH-129");
        FISH_95_LIST.add("FISH-130");
        FISH_95_LIST.add("FISH-131");
        FISH_95_LIST.add("FISH-132");
        PROP_EXCHANGE.put("FISH-133", FISH_133_LIST);

        List<String> FISH_125_LIST = new ArrayList<>(7);
        FISH_95_LIST.add("FISH-119");
        FISH_95_LIST.add("FISH-120");
        FISH_95_LIST.add("FISH-121");
        FISH_95_LIST.add("FISH-122");
        FISH_95_LIST.add("FISH-123");
        FISH_95_LIST.add("FISH-124");
        PROP_EXCHANGE.put("FISH-125", FISH_125_LIST);

    }
}
