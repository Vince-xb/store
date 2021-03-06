package com.taotao.manage.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taotao.common.bean.EasyUIResult;
import com.taotao.common.service.ApiService;
import com.taotao.manage.mapper.ItemMapper;
import com.taotao.manage.pojo.Item;
import com.taotao.manage.pojo.ItemCat;
import com.taotao.manage.pojo.ItemDesc;
import com.taotao.manage.pojo.ItemParam;
import com.taotao.manage.pojo.ItemParamItem;

@Service
public class ItemService extends BaseService<Item>{

    @Autowired
    private ItemDescService itemDescService;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ItemCatService itemCatService;
    @Autowired
    private ItemParamItemService itemParamItemService;
    @Autowired
    private ApiService apiService;
    @Autowired
    private ItemParamService itemParamService;
    
    
    @Value("${TAOTAO_WEB_URL}")
    private String TAOTAO_WEB_URL;
    
    public boolean saveItem(Item item, String desc, String itemParams) {
        //初始值
        item.setStatus(1);
        item.setId(null);//出于安全考虑，强制设置ID为null，通过数据库自增长得到。
        item.setCat(this.itemCatService.queryById(item.getCid()).getName());
        Integer count1 = super.save(item);

        ItemDesc itemDesc = new ItemDesc();
        itemDesc.setItemId(item.getId());
        itemDesc.setItemDesc(desc);

        ItemCat cat = this.itemCatService.queryById(item.getCid());
        item.setCat(cat.getName());

        Integer count2 = this.itemDescService.save(itemDesc);
        //保存规格参数数据
        ItemParamItem itemParamItem = new ItemParamItem();
        itemParamItem.setItemId(item.getId());
        if ("[]".equals(itemParams)) {
            itemParams = "[{\"group\":\"默认\",\"params\":[{\"k\":\"默认\",\"v\":\"0\"}]}]";
        }
        itemParamItem.setParamData(itemParams);
        Integer count3 = this.itemParamItemService.save(itemParamItem);
        return count1.intValue() == 1   && count2.intValue() == 1 && count3.intValue() == 1   ;
    }

    public EasyUIResult queryItemList(Integer page, Integer rows) {

        //设置分页参数
        PageHelper.startPage(page, rows);

        //
        Example example = new Example(Item.class);
        //按照创建时间
        example.setOrderByClause("created DESC");
        List<Item> items = this.itemMapper.selectByExample(example);
        PageInfo<Item> pageInfo = new PageInfo<>(items);
        return new EasyUIResult(pageInfo.getTotal(),pageInfo.getList());

    }

    public boolean updateItem(Item item, String desc, String itemParams) {
        //强制设置不能被修改
        item.setStatus(null);
        Integer count1 = super.updateSelective(item);

        ItemDesc itemDesc = new ItemDesc();
        itemDesc.setItemId(item.getId());
        itemDesc.setItemDesc(desc);
        Integer count2 = this.itemDescService.updateSelective(itemDesc);
        
        System.out.println(itemParams);
        //更新规格参数数据
        if ("[]".equals(itemParams)) {
            itemParams = "[{\"group\":\"1\",\"params\":[{\"k\":\"1\",\"v\":\"1\"}]},{\"group\":\"2\",\"params\":[{\"k\":\"2\",\"v\":\"1\"}]}]";
            ItemParam itemParam = new ItemParam();
            itemParam.setId(null);
            itemParam.setItemCatId(item.getCid());
            itemParam.setParamData(itemParams);
            itemParam.setItemCatName(this.itemCatService.queryById(item.getCid()).getName());
            this.itemParamService.save(itemParam);
            System.out.println(itemParams);
        }
        Integer count3 = this.itemParamItemService.updateItemParamItem(item.getId(),itemParams);
        
        try {
            //通知其他系统该商品已经更新
            String url = TAOTAO_WEB_URL + "/item/cache/" + item.getId() + ".html";
            System.out.println(url);
            this.apiService.doPost1(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(count2 + ":::::::" + count3);
        return count1.intValue() == 1 && count2.intValue() == 1 && count3.intValue() == 1;
    }


}
