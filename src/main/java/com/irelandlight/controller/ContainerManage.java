package com.irelandlight.controller;

import com.irelandlight.manager.ContextException;
import com.irelandlight.model.Goods;
import com.irelandlight.service.GoodsContainerService;
import com.irelandlight.util.ImgeUploadUtil;
import com.irelandlight.vo.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.BindException;
import java.util.*;

/**
 * Created by mac on 2016/12/13.
 *
 *货柜管理模块，根级url /container
 *
 */

@Controller
@RequestMapping("/container")
public class ContainerManage {
    //服务设置模块
    @Resource
    private GoodsContainerService containerService;
    //图片上传组件
    @Resource
    private ImgeUploadUtil imgeUploadUtil;

    /**
     * 七牛云外链地址
     */
    private final String QINIULINK="http://ohlu5erjk.bkt.clouddn.com/";


    /**
     * 展示商品页的基本商品数量统计信息
     * 包括：商品总数
     * 已上架商品
     * 未上架商品
     * 商品的品论总数
     */
    @RequestMapping(value = "/queryGoodsSimpleInfo",method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public  ItemsInfo showGoodsCountInfo(){
        ItemsInfo itemsInfo=containerService.searchForGoodsCountInfo();
        return itemsInfo;
    }


    /**
     * 展示商品页的基本商品数量统计信息,如果不是ajax请求则用这个方法
     * @param model
     * @return
     */
    @RequestMapping(value = "/queryGoodsInfo",method = {RequestMethod.GET,RequestMethod.POST})
    public ModelAndView showGoodsCountInfo(ModelAndView model){
        ItemsInfo itemsInfo=containerService.searchForGoodsCountInfo();

        model.addObject("itemsInfo",itemsInfo);
        model.setViewName("");  //待填充,需要前端的页面名称
        return model;
    }


    /**
     * 查询未上架商品列表在商品上架选项卡显示
     * @return List<ContainerItem> 商品信息列表
     */
    @RequestMapping(value = "/queryUnputawayGoods",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public List<ContainerItem> showUnputawayGoods(){
        List<ContainerItem> containerItemList=null;
        containerItemList=containerService.searchForUnPutawayGoods();
        return containerItemList;
    }

    /**
     * 展示已经上架的商品该要信息列表
     * @return
     */
    @RequestMapping(value = "/queryPutawayGoods",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public List<ContainerItem> showputawayGoods(){
        List<ContainerItem> containerItemList=null;
        containerItemList=containerService.searchForPutawayGoods();
        return containerItemList;
    }


    /**
     * 查找商品详情，绑定参数为上下架标志和商品的Id
     * @param goodsId
     * @param pwFlag
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryGoodsById",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public GoodsDetail showGoodsDetailById(@RequestParam(value = "goodsId",required = true) Long  goodsId,int pwFlag)throws Exception{
        GoodsDetail goodsDetail=null;
        goodsDetail=containerService.queryGoodsById(goodsId,pwFlag);
        return goodsDetail;
    }


    /**
     * 批量上架，上架成功响应成功，上架失败响应失败
     * @param goodsVOS
     * @param resp
     * @throws Exception
     */
    @RequestMapping(value = "/putawayAllGoods",method = {RequestMethod.POST})
    public void putawayAllGoods(@RequestBody List<ModifyGoodsVO> goodsVOS, HttpServletResponse resp)throws Exception{
        Map<Long,List<String>> map=new HashMap<Long, List<String>>();
        String message="Modify fail!";
        if(goodsVOS!=null&&goodsVOS.size()!=0) {
            for (ModifyGoodsVO goodsVO:goodsVOS) {
                List<String> sizes=new LinkedList<String>(goodsVO.getSizeMapPrice().keySet());
                map.put(goodsVO.getGoodsId(),sizes);
            }
        }
        message=containerService.putawayAllGoods(map)?"Modify success":message;
        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        resp.getWriter().println(message);
    }

    /**
     *  批量下架，下架成功返回成功，下架失败返回失败
     * @param goodsVOS
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/saleoutAllGoods",method = {RequestMethod.POST})
    @ResponseBody
    public String saleOutAllGoods(@RequestBody List<ModifyGoodsVO> goodsVOS)throws Exception{
        Map<Long,List<String>> map=new HashMap<Long, List<String>>();
        String message="Modify fail!";
        if(goodsVOS!=null&&goodsVOS.size()!=0) {
            for (ModifyGoodsVO goodsVO:goodsVOS) {
                List<String> sizes=new LinkedList<String>(goodsVO.getSizeMapPrice().keySet());
                map.put(goodsVO.getGoodsId(),sizes);
            }
        }
        message=containerService.saleoutAllGoods(map)?"Modify success":message;
        return message;
    }

    /**
     *  将参数绑定到map时map必须是某个类的子对象，且在前端页面上以 map[key]-value的形式完成前端页面的设置
     * @param goods
     * @param goodsSizeMapPrice
     * @param goodImgs
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/addGoods",method={RequestMethod.POST})
    @ResponseBody
    public String addGoods(Goods goods, ModifyGoodsVO goodsSizeMapPrice,@RequestParam("goodsImgs") MultipartFile[] goodImgs)throws Exception{
        String message="Add fail!";
        if(!paramValidate(goods)) return "Parameter Error";
        if(!paramValidate(goodsSizeMapPrice)) return "Parameter Error";
        Map<String,Integer>  goodsImgPos=new TreeMap<String, Integer>();
        String imgName=null;
        for(int i=0;i<goodImgs.length;i++){
            imgName=goodImgs[i].getOriginalFilename();
            if(imgName==null) return "商品图片名称出错";
            imgName=UUID.randomUUID()+imgName.substring(imgName.lastIndexOf("."));
            imgeUploadUtil.upLoad(goodImgs[i].getInputStream(),imgName);
            goodsImgPos.put(QINIULINK+imgName,i);
        }
        if(containerService.addGoods(goods,goodsImgPos,goodsSizeMapPrice.getSizeMapPrice())) message="Add success";
        return message;
    }

    /**
     * 商品信息修改
     * @param goods
     * @param goodsSizeMapPrice
     * @param modifyImgVo
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/modifyGoodsInfo",method = RequestMethod.POST)
    @ResponseBody
    public String modifyGoodsInfo(Goods goods, ModifyGoodsVO goodsSizeMapPrice, ModifyImgVo modifyImgVo)throws Exception{
        String message="Modify fail!";
        if(!paramValidate(goods)) return "Parameter Error";
        if(!paramValidate(goods)) return "Parameter Error";
        if(!paramValidate(modifyImgVo)) return "Parameter Error";
        if(containerService.modifyGoodsInfoLong(goods,modifyImgVo.getImgMapPos(),goodsSizeMapPrice.getSizeMapPrice()))
            message="Modify success!";
        return message;
    }


    @RequestMapping(value = "/inquire" ,method = {RequestMethod.GET,RequestMethod.POST})
    public void resp(HttpServletResponse servletResponse, HttpServletRequest servletRequest)throws Exception{
        String temp=
                " [{\n" +
                        "\t\t\t\t\"Pic\":\"img/A.jpg\",\n" +
                        "\t\t\t\t\"address\":\"A号楼\",\n" +
                        "\t\t\t\t\"time\":\"2014-10-10\",\n" +
                        "\t\t\t\t\"taskNo\":\"000001\",\n" +
                        "\t\t\t\t\"state\":4},\n" +
                        "\t\t\t\t{\n" +
                        "\t\t\t\t\"Pic\":\"B.jpg\",\n" +
                        "\t\t\t\t\"address\":\"B号楼\",\n" +
                        "\t\t\t\t\"time\":\"2014-11-10\",\n" +
                        "\t\t\t\t\"taskNo\":\"000002\",\n" +
                        "\t\t\t\t\"state\":4},\n" +
                        "\t\t\t\t{\n" +
                        "\t\t\t\t\"Pic\":\"C.jpg\",\n" +
                        "\t\t\t\t\"address\":\"C号楼\",\n" +
                        "\t\t\t\t\"time\":\"2014-11-10\",\n" +
                        "\t\t\t\t\"taskNo\":\"000003\",\n" +
                        "\t\t\t\t\"state\":5\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t]";
        String msg=servletRequest.getParameter("taskNo");


        if(msg!=null){
            System.out.println(msg);
        }
        servletResponse.setContentType("text/html;charset=utf-8");
        servletResponse.setHeader("Access-Control-Allow-Origin","*");
        servletResponse.setCharacterEncoding("utf-8");
        servletResponse.getWriter().println(temp);
    }
    @RequestMapping(value = "/taskManagement" ,method = {RequestMethod.GET,RequestMethod.POST})
    public void resp2(HttpServletResponse servletResponse,HttpServletRequest servletRequest)throws Exception{
        String temp=" [{\n" +
                "\t\t\t\t\t\"Pic\":\"img/A.jpg\",\n" +
                "\t\t\t\t\t\"address\":\"A号楼\",\n" +
                "\t\t\t\t\t\"time\":\"2014-10-10\",\n" +
                "\t\t\t\t\t\"taskNo\":\"000001\",\n" +
                "\t\t\t\t\t\"state\":1},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\"Pic\":\"B.jpg\",\n" +
                "\t\t\t\t\t\"address\":\"B号楼\",\n" +
                "\t\t\t\t\t\"time\":\"2014-11-10\",\n" +
                "\t\t\t\t\t\"taskNo\":\"000002\",\n" +
                "\t\t\t\t\t\"state\":2},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\"Pic\":\"C.jpg\",\n" +
                "\t\t\t\t\t\"address\":\"C号楼\",\n" +
                "\t\t\t\t\t\"time\":\"2014-11-10\",\n" +
                "\t\t\t\t\t\"taskNo\":\"000003\",\n" +
                "\t\t\t\t\t\"state\":1\n" +
                "\t\t\t\t\t}]";

        String msg=servletRequest.getParameter("taskNo");


        if(msg!=null){
            System.out.println(msg);
        }
        servletResponse.setContentType("text/html;charset=utf-8");
        servletResponse.setHeader("Access-Control-Allow-Origin","*");
        servletResponse.setCharacterEncoding("utf-8");
        servletResponse.getWriter().println(temp);
    }





    @RequestMapping(value = "/police" ,method = {RequestMethod.GET,RequestMethod.POST})
    public void resp3(HttpServletResponse servletResponse,HttpServletRequest servletRequest)throws Exception{
        String temp=" [{\n" +
                "\t\t\t\t\"Pic\":\"img/A.jpg\",\n" +
                "\t\t\t\t\"address\":\"A号楼\",\n" +
                "\t\t\t\t\"time\":\"2014-10-10\",\n" +
                "\t\t\t\t\"taskNo\":\"000001\",\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\"Pic\":\"B.jpg\",\n" +
                "\t\t\t\t\"address\":\"B号楼\",\n" +
                "\t\t\t\t\"time\":\"2014-11-10\",\n" +
                "\t\t\t\t\"taskNo\":\"000005\",\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\"Pic\":\"C.jpg\",\n" +
                "\t\t\t\t\"address\":\"C号楼\",\n" +
                "\t\t\t\t\"time\":\"2014-11-10\",\n" +
                "\t\t\t\t\"taskNo\":\"000007\",\n" +
                "\t\t\t\t}\n" +
                "\t\t\t]";
        String msg=servletRequest.getParameter("taskNo");


        if(msg!=null){
            System.out.println(msg);
        }
        servletResponse.setContentType("text/html;charset=utf-8");
        servletResponse.setHeader("Access-Control-Allow-Origin","*");
        servletResponse.setCharacterEncoding("utf-8");
        servletResponse.getWriter().println(temp);
    }


    @RequestMapping(value = "/dangerDetail" ,method = {RequestMethod.GET,RequestMethod.POST})
    public void resp4(HttpServletResponse servletResponse,HttpServletRequest servletRequest)throws Exception{
        String temp="{\n\"itemNo\":\"521134\",\n" +

                "\t\"pic\":\"http://pic32.nipic.com/20130827/12906030_123121414000_2.png\",\n" +
                "\t\"title\":\"A号楼拐角\",\n" +
                "\t\"descript\":\"第三代不会变粗大洪水杀病毒合并时\",\n" +
                "\t\"list\":[\n" +
                "\t\t\"http://pic32.nipic.com/20130827/12906030_123121414000_2.png\",\n" +
                "\t\t\"补充气体存储量\",\n" +
                "\t\t\"http://pic32.nipic.com/20130827/12906030_123121414000_2.png\",\n" +
                "\t\t\"互不好吧湖北\"\n" +
                "\t]\n" +
                "}";
        servletResponse.setContentType("text/html;charset=utf-8");
        servletResponse.setHeader("Access-Control-Allow-Origin","*");
        servletResponse.setCharacterEncoding("utf-8");
        servletResponse.getWriter().println(temp);
    }


    @RequestMapping(value = "/fix" ,method = {RequestMethod.GET,RequestMethod.POST})
    public void resp5(HttpServletResponse servletResponse,HttpServletRequest servletRequest)throws Exception{
        String temp="success";
        String msg=servletRequest.getParameter("itemNo");

        if(msg!=null){
            System.out.println(msg);
        }
        servletResponse.setContentType("text/html;charset=utf-8");
        servletResponse.setHeader("Access-Control-Allow-Origin","*");
        servletResponse.setCharacterEncoding("utf-8");
        servletResponse.getWriter().println(temp);
    }

    /**
     * 货柜管理异常处理
     * @param ex
     * @return
     */
    @ExceptionHandler(value = {ContextException.class,RuntimeException.class, BindException.class})
    @ResponseBody
    public Map<String,Object> goodsExcrptionHandler(Exception ex){
        Map<String,Object> model=new TreeMap<String,Object>();
        model.put("error:",ex.getMessage());
        return model;
    }

    /**
     * 参数效验
     * @param goods
     * @return
     */
    private boolean paramValidate(Goods goods){
        if(goods == null) return false;
        if(goods.getName()==null) return false;
        if(goods.getDescription()==null) return false;
        if(goods.getPreference()==null) return false;
        if(goods.getQuantity()==null) return false;
        if(goods.getSaleCount()==null) return false;
        if(goods.getUse()==null) return false;
        if(goods.getTaste()==null) return false;
        if(goods.getStatus()==null) return false;
        if(goods.getWeight()==null) return false;
        return true;
    }

    /**
     * 参数效验
     * @param goodsSizeMapPrice
     * @return
     */
    private boolean paramValidate(ModifyGoodsVO goodsSizeMapPrice){
        if(goodsSizeMapPrice==null||goodsSizeMapPrice.getSizeMapPrice()==null
                ||goodsSizeMapPrice.getSizeMapPrice().size()==0)
                        return false;
        return true;
    }


    /**
     * 参数效验
     * @param modifyImgVo
     * @return
     */
    private boolean paramValidate(ModifyImgVo modifyImgVo){
        if(modifyImgVo==null||modifyImgVo.getImgMapPos()==null
                ||modifyImgVo.getImgMapPos().size()==0)
            return false;
        return true;
    }



}
