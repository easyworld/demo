package com.example.demo.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class Main6 {
    static String basePath = "https://raw.githubusercontent.com/MyShiLingStar/ACNHPokerCore/main/ACNHPokerCore";
    static String _remake_ = "_Remake_";
    static String urlPrefix = "https://acnhcdn.com/latest/FtrIcon/";

    public static void main(String[] args) throws Exception {
        // id ; iName ; eng ; jpn ; tchi ; schi ; kor ; fre ; ger ; spa ; ita ; dut ; rus ;
        List<Map<String, String>> variations = parsePokerCSV(readAll(basePath + "/csv/variations.csv"));
        // key:iName value:main_sub
        Map<String, String> varNumMap = getVarNumMap(variations.stream().map(m -> m.get("iName")).collect(Collectors.toSet()));
        // id ; iName ; eng ; jpn ; tchi ; schi ; kor ; fre ; ger ; spa ; ita ; dut ; rus ; color ; size ;
        List<Map<String, String>> items = parsePokerCSV(readAll(basePath + "/csv/items.csv"));
        Map<String, String> idSchiMap = items.stream().collect(Collectors.toMap(m -> m.get("id"), m -> m.get("schi")));
        // id,iName,overrideName
        List<Map<String, String>> override = parsePokerCSV(readAll(basePath + "/csv/override.csv"));
        // iName,overrideName
        Map<String, String> overrideMap = override.stream().collect(Collectors.toMap(m -> m.get("iName"), m -> m.get("overrideName")));
        // id ; kind ;
        List<Map<String, String>> kind = parsePokerCSV(readAll(basePath + "/csv/kind.csv"));
        // id ; kind ;
        Map<String, String> kindMap = kind.stream().collect(Collectors.toMap(m -> m.get("id"), m -> m.get("kind")));
        // id ; iName ; eng ; jpn ; tchi ; schi ; kor ; fre ; ger ; spa ; ita ; dut ; rus ;
        List<Map<String, String>> recipes = parsePokerCSV(readAll(basePath + "/csv/recipes.csv"));


        String[] resultColumn = new String[]{"id", "hexValue", "path", "extra"};
        Set<String> skipKind = new HashSet<>();
        skipKind.add("Kind_Tree");
        skipKind.add("Kind_Bush");
        skipKind.add("Kind_VegeTree");
        skipKind.add("Kind_FlowerBud");
        skipKind.add("Kind_GardenEditList");


        // id ; hexValue ; path
        List<Map<String, String>> result = new ArrayList<>();
        // items
        for (Map<String, String> item : items) {
            if (skipKind.contains(kindMap.get(item.get("id")))) continue;
            Map<String, String> map = new LinkedHashMap<>();
            String id = item.get("id");
            map.put(resultColumn[0], id);

            String iName = item.get("iName");
            // var type
            if (varNumMap.containsKey(iName)) {
                int main = Integer.parseInt(varNumMap.get(iName).split("_")[0]);
                int sub = Integer.parseInt(varNumMap.get(iName).split("_")[1]);
                for (int i = 0; i <= sub; i++) {
                    for (int j = 0; j <= main; j++) {
                        Map<String, String> tempMap = new LinkedHashMap<>(map);
                        tempMap.put(resultColumn[1], String.valueOf(i * 0x20 + j));
                        tempMap.put(resultColumn[2], iName + _remake_ + j + "_" + i + ".png");
                        map.put(resultColumn[3], "");
                        result.add(tempMap);
                    }
                }
                continue;
            }

            // maxNum
            Integer num = kindNumMap.get(kindMap.get(id));
            if (num != null) {
                map.put(resultColumn[1], String.valueOf(num - 1));
            } else {
                map.put(resultColumn[1], "0");
            }

            // override
            if (overrideMap.containsKey(iName)) {
                String overrideIName = overrideMap.get(iName);
                map.put(resultColumn[2], overrideIName + ".png");
            } else {
                map.put(resultColumn[2], iName + ".png");
            }
            if (num != null) {
                map.put(resultColumn[3], String.valueOf(num));
            }
            result.add(map);
        }

        // recipes
        for (Map<String, String> recipe : recipes) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put(resultColumn[0], "16A2");
            map.put(resultColumn[1], String.valueOf(Integer.parseInt(recipe.get("id"), 16)));
            String iName = recipe.get("iName");

            if (varNumMap.containsKey(iName)) { // var type
                map.put(resultColumn[2], iName + _remake_ + "0_0" + ".png");
            } else if (overrideMap.containsKey(iName)) { // override
                String overrideIName = overrideMap.get(iName);
                map.put(resultColumn[2], overrideIName + ".png");
            } else {
                map.put(resultColumn[2], iName + ".png");
            }
            map.put(resultColumn[3], "DIY");
            result.add(map);
        }
        /* check if you are not sure
        for (Map<String, String> map : result) {
            String fileName = map.get(resultColumn[2]);
            File f = new File(basePath + "/img/" + fileName);
            if (!f.exists()) {
                System.out.println("ERROR " + kindMap.get(map.get("id")) + " " + map);
                System.exit(1);
            }
        }*/

        // show result
        try (PrintWriter pw = new PrintWriter("result.txt")) {
            for (Map<String, String> map : result) {
                String s = idSchiMap.get(map.get(resultColumn[0]));
                if (s == null) {
                    String s1 = map.get(resultColumn[1]);
                    s = recipes.stream().collect(Collectors.toMap(m -> m.get("id"), m -> m.get("schi"))).get(String.format("%04X", Integer.parseInt(s1)));
                }
                pw.println(s + "\t" + map.get(resultColumn[0]) + "\t" + map.get(resultColumn[1])
                        + "\t" + urlPrefix + map.get(resultColumn[2]) + "\t"
                        + map.getOrDefault(resultColumn[3], ""));
            }
        }
    }

    /**
     * hexValue = i + j * 0x20
     * i ∈ [0,main], j ∈ [0,sub]
     *
     * @param iNames iNames
     * @return key:iName value:main_sub
     */
    private static Map<String, String> getVarNumMap(Set<String> iNames) {
        File f = new File(basePath + "/img");
        Map<String, String> varNumMap = new LinkedHashMap<>();
        Arrays.stream(Objects.requireNonNull(f.list((dir, name) -> name.contains(_remake_)))).forEach(str -> {
            if (iNames.contains(str.split(_remake_)[0])) {
                varNumMap.put(str.split(_remake_)[0], str.split(_remake_)[1].split("\\.")[0]);
            }
        });
        return varNumMap;
    }

    private static List<String> readAll(String path) throws Exception {
        List<String> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            for (String str = br.readLine(); str != null; str = br.readLine()) {
                result.add(str);
            }
        }
        return result;
    }


    private static List<Map<String, String>> parsePokerCSV(List<String> pokerCsvString) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        String regex = " ; ";
        String[] columns = pokerCsvString.get(0).split(regex);
        for (int i = 1; i < pokerCsvString.size(); i++) {
            Map<String, String> row = new LinkedHashMap<>();
            String[] rowData = pokerCsvString.get(i).split(regex);
            for (int j = 0; j < columns.length; j++) {
                if (columns[j].length() == 0) continue;
                if (j >= rowData.length) continue;
                row.put(columns[j], rowData[j]);
            }
            result.add(row);
        }
        return result;
    }

    static Map<String, Integer> kindNumMap = new HashMap<>();

    // data from https://github.com/MyShiLingStar/ACNHPokerCore/blob/3b554fd060b9243edc6d5e89090332cfa7d678af/ACNHPokerCore/Utilities/Utilities.cs#L4301
    static {
        kindNumMap.put("Kind_Ftr", 1);
        kindNumMap.put("Kind_Dishes", 1);
        kindNumMap.put("Kind_Drink", 1);
        kindNumMap.put("Kind_CookingMaterial", 50);
        kindNumMap.put("Kind_RoomWall", 1);
        kindNumMap.put("Kind_RoomFloor", 1);
        kindNumMap.put("Kind_Rug", 1);
        kindNumMap.put("Kind_RugMyDesign", 1);
        kindNumMap.put("Kind_Socks", 1);
        kindNumMap.put("Kind_Cap", 1);
        kindNumMap.put("Kind_Helmet", 1);
        kindNumMap.put("Kind_Accessory", 1);
        kindNumMap.put("Kind_Bag", 1);
        kindNumMap.put("Kind_Umbrella", 1);
        kindNumMap.put("Kind_FtrWall", 1);
        kindNumMap.put("Kind_Counter", 1);
        kindNumMap.put("Kind_Pillar", 1);
        kindNumMap.put("Kind_FishingRod", 1);
        kindNumMap.put("Kind_Net", 1);
        kindNumMap.put("Kind_Shovel", 1);
        kindNumMap.put("Kind_Axe", 1);
        kindNumMap.put("Kind_Watering", 1);
        kindNumMap.put("Kind_Slingshot", 1);
        kindNumMap.put("Kind_ChangeStick", 1);
        kindNumMap.put("Kind_WoodenStickTool", 1);
        kindNumMap.put("Kind_Ladder", 1);
        kindNumMap.put("Kind_GroundMaker", 1);
        kindNumMap.put("Kind_RiverMaker", 1);
        kindNumMap.put("Kind_CliffMaker", 1);
        kindNumMap.put("Kind_HandBag", 1);
        kindNumMap.put("Kind_PartyPopper", 10);
        kindNumMap.put("Kind_Ocarina", 1);
        kindNumMap.put("Kind_Panflute", 1);
        kindNumMap.put("Kind_Tambourine", 1);
        kindNumMap.put("Kind_MaracasCarnival", 1);
        kindNumMap.put("Kind_StickLight", 1);
        kindNumMap.put("Kind_StickLightColorful", 1);
        kindNumMap.put("Kind_Uchiwa", 1);
        kindNumMap.put("Kind_SubToolSensu", 1);
        kindNumMap.put("Kind_Windmill", 1);
        kindNumMap.put("Kind_Partyhorn", 1);
        kindNumMap.put("Kind_BlowBubble", 10);
        kindNumMap.put("Kind_FierworkHand", 10);
        kindNumMap.put("Kind_Balloon", 1);
        kindNumMap.put("Kind_HandheldPennant", 1);
        kindNumMap.put("Kind_BigbagPresent", 1);
        kindNumMap.put("Kind_JuiceFuzzyapple", 1);
        kindNumMap.put("Kind_Megaphone", 1);
        kindNumMap.put("Kind_SoySet", 1);
        kindNumMap.put("Kind_FlowerShower", 1);
        kindNumMap.put("Kind_Candyfloss", 1);
        kindNumMap.put("Kind_SubToolDonut", 1);
        kindNumMap.put("Kind_SubToolEat", 1);
        kindNumMap.put("Kind_SubToolEatRemakeable", 1);
        kindNumMap.put("Kind_Tapioca", 1);
        kindNumMap.put("Kind_SubToolCan", 1);
        kindNumMap.put("Kind_Icecandy", 1);
        kindNumMap.put("Kind_SubToolIcecream", 1);
        kindNumMap.put("Kind_SubToolIcesoft", 1);
        kindNumMap.put("Kind_SubToolEatDrop", 1);
        kindNumMap.put("Kind_SubToolGeneric", 1);
        kindNumMap.put("Kind_Basket", 1);
        kindNumMap.put("Kind_Lantern", 1);
        kindNumMap.put("Kind_SubToolRemakeable", 1);
        kindNumMap.put("Kind_Timer", 1);
        kindNumMap.put("Kind_Gyroid", 1);
        kindNumMap.put("Kind_GyroidScrap", 1);
        kindNumMap.put("Kind_TreeSeedling", 10);
        kindNumMap.put("Kind_Tree", 1);
        kindNumMap.put("Kind_BushSeedling", 10);
        kindNumMap.put("Kind_Bush", 1);
        kindNumMap.put("Kind_VegeSeedling", 10);
        kindNumMap.put("Kind_VegeTree", 1);
        kindNumMap.put("Kind_Vegetable", 10);
        kindNumMap.put("Kind_Weed", 99);
        kindNumMap.put("Kind_WeedLight", 50);
        kindNumMap.put("Kind_FlowerSeed", 10);
        kindNumMap.put("Kind_FlowerBud", 1);
        kindNumMap.put("Kind_Flower", 10);
        kindNumMap.put("Kind_Fruit", 10);
        kindNumMap.put("Kind_Mushroom", 10);
        kindNumMap.put("Kind_Turnip", 1);
        kindNumMap.put("Kind_TurnipExpired", 1);
        kindNumMap.put("Kind_FishBait", 10);
        kindNumMap.put("Kind_PitFallSeed", 10);
        kindNumMap.put("Kind_Medicine", 10);
        kindNumMap.put("Kind_CraftMaterial", 30);
        kindNumMap.put("Kind_CraftRemake", 50);
        kindNumMap.put("Kind_Ore", 30);
        kindNumMap.put("Kind_CraftPhoneCase", 1);
        kindNumMap.put("Kind_Honeycomb", 10);
        kindNumMap.put("Kind_Trash", 1);
        kindNumMap.put("Kind_SnowCrystal", 10);
        kindNumMap.put("Kind_AutumnLeaf", 10);
        kindNumMap.put("Kind_Sakurapetal", 10);
        kindNumMap.put("Kind_XmasDeco", 10);
        kindNumMap.put("Kind_StarPiece", 10);
        kindNumMap.put("Kind_Insect", 1);
        kindNumMap.put("Kind_Fish", 1);
        kindNumMap.put("Kind_DiveFish", 1);
        kindNumMap.put("Kind_ShellDrift", 10);
        kindNumMap.put("Kind_ShellFish", 1);
        kindNumMap.put("Kind_FishToy", 1);
        kindNumMap.put("Kind_InsectToy", 1);
        kindNumMap.put("Kind_Fossil", 1);
        kindNumMap.put("Kind_FossilUnknown", 1);
        kindNumMap.put("Kind_Music", 1);
        kindNumMap.put("Kind_MusicMiss", 1);
        kindNumMap.put("Kind_Bromide", 1);
        kindNumMap.put("Kind_Poster", 1);
        kindNumMap.put("Kind_HousePost", 1);
        kindNumMap.put("Kind_DoorDeco", 1);
        kindNumMap.put("Kind_Fence", 50);
        kindNumMap.put("Kind_DummyRecipe", 1);
        kindNumMap.put("Kind_DummyDIYRecipe", 1);
        kindNumMap.put("Kind_DummyHowtoBook", 1);
        kindNumMap.put("Kind_LicenseItem", 1);
        kindNumMap.put("Kind_BridgeItem", 1);
        kindNumMap.put("Kind_SlopeItem", 1);
        kindNumMap.put("Kind_DIYRecipe", 1);
        kindNumMap.put("Kind_MessageBottle", 1);
        kindNumMap.put("Kind_WrappingPaper", 10);
        kindNumMap.put("Kind_Otoshidama", 10);
        kindNumMap.put("Kind_HousingKit", 1);
        kindNumMap.put("Kind_HousingKitRcoQuest", 1);
        kindNumMap.put("Kind_HousingKitBirdge", 1);
        kindNumMap.put("Kind_Money", 1);
        kindNumMap.put("Kind_FireworkM", 1);
        kindNumMap.put("Kind_BdayCupcake", 10);
        kindNumMap.put("Kind_YutaroWisp", 5);
        kindNumMap.put("Kind_JohnnyQuest", 10);
        kindNumMap.put("Kind_JohnnyQuestDust", 10);
        kindNumMap.put("Kind_PirateQuest", 10);
        kindNumMap.put("Kind_QuestWrapping", 1);
        kindNumMap.put("Kind_QuestChristmasPresentbox", 1);
        kindNumMap.put("Kind_LostQuest", 1);
        kindNumMap.put("Kind_LostQuestDust", 1);
        kindNumMap.put("Kind_TailorTicket", 10);
        kindNumMap.put("Kind_TreasureQuest", 1);
        kindNumMap.put("Kind_TreasureQuestDust", 1);
        kindNumMap.put("Kind_MilePlaneTicket", 10);
        kindNumMap.put("Kind_RollanTicket", 5);
        kindNumMap.put("Kind_EasterEgg", 30);
        kindNumMap.put("Kind_LoveCrystal", 30);
        kindNumMap.put("Kind_Candy", 30);
        kindNumMap.put("Kind_HarvestDish", 1);
        kindNumMap.put("Kind_Feather", 3);
        kindNumMap.put("Kind_RainbowFeather", 1);
        kindNumMap.put("Kind_Vine", 30);
        kindNumMap.put("Kind_SettingLadder", 1);
        kindNumMap.put("Kind_SincerityTowel", 1);
        kindNumMap.put("Kind_SouvenirChocolate", 10);
        kindNumMap.put("Kind_Giftbox", 1);
        kindNumMap.put("Kind_PinataStick", 1);
        kindNumMap.put("Kind_NpcOutfit", 1);
        kindNumMap.put("Kind_PlayerDemoOutfit", 1);
        kindNumMap.put("Kind_Picture", 1);
        kindNumMap.put("Kind_Sculpture", 1);
        kindNumMap.put("Kind_PictureFake", 1);
        kindNumMap.put("Kind_SculptureFake", 1);
        kindNumMap.put("Kind_SmartPhone", 1);
        kindNumMap.put("Kind_DummyFtr", 1);
        kindNumMap.put("Kind_SequenceOnly", 1);
        kindNumMap.put("Kind_MyDesignObject", 1);
        kindNumMap.put("Kind_MyDesignTexture", 1);
        kindNumMap.put("Kind_CommonFabricRug", 1);
        kindNumMap.put("Kind_CommonFabricObject", 1);
        kindNumMap.put("Kind_CommonFabricTexture", 1);
        kindNumMap.put("Kind_OneRoomBox", 1);
        kindNumMap.put("Kind_DummyWrapping", 1);
        kindNumMap.put("Kind_DummyPresentbox", 1);
        kindNumMap.put("Kind_DummyCardboard", 1);
        kindNumMap.put("Kind_EventObjFtr", 1);
        kindNumMap.put("Kind_NnpcRoomMarker", 1);
        kindNumMap.put("Kind_PhotoStudioList", 1);
        kindNumMap.put("Kind_ShopTorso", 1);
        kindNumMap.put("Kind_DummyWrappingOtoshidama", 1);
        kindNumMap.put("Kind_GardenEditList", 1);
    }
}
