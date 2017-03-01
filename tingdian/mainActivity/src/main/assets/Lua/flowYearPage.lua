require ("public")
local userInfoSP
local imagePopupView
local mobileNum
local picturePath=""

local planID=""
local plan2ID=""
local planPrice=""
local planName=""
local isRecom
local commodityId

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  imagePopupView=page:getElementById("image_popupID")
  
  isRecom=page:getArguments():getString("isRecommend")
  commodityId=page:getArguments():getString("commodityId")
  
  customerDetail(page)
end

function onPageResume(page)
  local signaturePath=page:getArguments():getString("signaturePath")
  if signaturePath ~= getElementValue(page,"signature_urlID") and signaturePath ~= nil then
    setElementValue(page,"signature_urlID",signaturePath)
    setElementValue(page,"signatureID","local://"..signaturePath)
  end
end


-----^_^.^_^.^_^.^_^.^_^-------------小左边-------------^_^.^_^.^_^.^_^.^_^-----

function customerDetail(page)
  mobileNum=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
  local customerBrand=page:getActivity():decrypt(userInfoSP:getString("brand",""))
  
  setElementValue(page,"customer_mobileID",mobileNum)
  setElementValue(page,"customer_brandID",customerBrand)
  
--  --判断入口，service为客户服务——推荐业务，其他情况为首页其他业务
--  local entrance=page:getArguments():getString("entrance")
--  if entrance == "service" then
--    planID=page:getArguments():getString("planID")
--    
--    setElementHidden(page,"group_apply1ID",true)
--    setElementHidden(page,"group_apply2ID",false)
--    onPaymentSelected(page,posButton,"pos")
--    onStartImageResult(page)     
--  else
    setElementHidden(page,"group_apply1ID",false)
    setElementHidden(page,"group_apply2ID",true)
    onFlowYear(page)
--  end
end


-----^_^.^_^.^_^.^_^.^_^-------------步骤1-------------^_^.^_^.^_^.^_^.^_^-----

--流量年包列表
function onFlowYear(page)
  local Parameters={
    NetSelector="NetData:",                
    URL="/fluxPackage/openFluxPackage2.do",
    LuaNetCallBack="onFlowYearNetCallBack(page,{response})",
    LuaExceptionCallBack="onFlowYearExceptionCallBack(page)",
    RequestParams={},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);
end

local planData
function onFlowYearNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    planData=data
    
    local mealList=""
    for i=1,#data,1 do
      mealList=mealList..data[i]["MonthPackage"]..","
    end
    mealList=page:getActivity():getSpinnerList(mealList)
    
    onMealList(page,mealList)
    onPlanList(page,data)    
  else  
    page:showToast(response["message"])
  end
end

function onFlowYearExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--月套餐列表
function onMealList(page,mealList)
  mealList=LuaRemove(mealList," ")
  mealList=string.sub(mealList,2,string.len(mealList)-1)
  mealList=split(mealList,",")
  
  local listView=page:getElementById("meal_listID")
  
  listView:getEntity():clearTemplateData()
  local entityData={}
  for i=1,#mealList,1 do
    entityData[i]={subEntityValues={meal_nameID=mealList[i].."元/月"}}
  end
  listView:upDateEntity(entityData) 
end

--月套餐选择
local nextMeal
function onMealSelected(page,this)
  local mealName=this:getRootView():getElementById("meal_nameID"):getValue()

  if nextMeal ~= nil and nextMeal ~= this then
    nextMeal:setSelected(false)
  end
  nextMeal=this
  this:setSelected(true)
  
  onScreenList(page,mealName)
end

function onPlanList(page,data)
  local listView=page:getElementById("plan_listID")
  listView:getEntity():clearTemplateData()
  local entityData={}
  for i=1,#data,1 do
    entityData[i]={subEntityValues={plan_ID=data[i]["CospCode"],plan2_ID=data[i]["ProdCode"],plan_priceID=data[i]["Price"],plan_nameID=data[i]["Instruction"]},attributes={selected="false"}}
  end
  listView:upDateEntity(entityData) 
end

--流量年包选择
local nextPlan
function onPlanSelected(page,this)
  planID=this:getRootView():getElementById("plan_ID"):getValue()
  plan2ID=this:getRootView():getElementById("plan2_ID"):getValue()
  planPrice=this:getRootView():getElementById("plan_priceID"):getValue()
  planName=this:getRootView():getElementById("plan_nameID"):getValue()
  
  if nextPlan ~= nil and nextPlan ~= this then
    nextPlan:setSelected(false)
  end
  nextPlan=this
  this:setSelected(true)
  
  setElementEnable(page,"btn_dealWithID",false)
end

--选择月套餐后的筛选结果
function onScreenList(page,mealName)
  local listView=page:getElementById("plan_listID")
  listView:getEntity():clearTemplateData()
  local entityData={}
  for i=1,#planData,1 do
    if mealName == planData[i]["MonthPackage"].."元/月" then
      entityData[i]={subEntityValues={plan_ID=planData[i]["CospCode"],plan_priceID=planData[i]["Price"],plan_nameID=planData[i]["Instruction"]},attributes={selected="false"}}
    end
  end
  listView:upDateEntity(entityData)
  
  planID=""
  plan2ID=""
  planPrice=""
  planName=""
  setElementEnable(page,"btn_dealWithID",true)
end

--马上办理    
function onDealWith(page)
  setElementHidden(page,"group_apply1ID",true)
  setElementHidden(page,"group_apply2ID",false)
  setElementValue(page,"selected_planNameID",planName)
  onStartImageResult(page)
end

function onStartImageResult(page)
  local listView=page:getElementById("picture_listID")
  listView:getEntity():getTemplateData():clear()
  local entityData={}
  for i=1,1,1 do
    entityData[i]={subEntityValues={picture_typeID="photograph",pictureID="res://icon_picture_add.png"}}
  end
  listView:upDateEntity(entityData)
end

    
-----^_^.^_^.^_^.^_^.^_^-------------步骤2-------------^_^.^_^.^_^.^_^.^_^-----

--业务单详情
function onProtocol(page)
  local managers=page:getActivity():decrypt(userInfoSP:getString("managers",""))
  local signatureURL=getElementValue(page,"signature_urlID")
  local bundle=page:initBundle()
    bundle:putString("protocol","flowYear")
    bundle:putString("planID",planID)
    bundle:putString("plan2ID",plan2ID)
    page:setCustomAnimations("left")
    if managers == "true" then
      page:nextPage("res://protocolPage.xml",false,bundle) 
    elseif managers == "false" then
      page:nextPage("res://managersPage.xml",false,bundle) 
    end    
end

--拍照
function onPicture(page,this)
  local isPhotograph=this:getElementById("picture_typeID"):getValue()
  if isPhotograph == "photograph" then
    page:getActivity():photograph(mobileNum,"FluxYearPackage")
  elseif isPhotograph == "looking" then
    onBigImage(page)
  end
end

function ImageResult(page)
  local path=page:getActivity():getPhotoName()
  picturePath=picturePath..path..","
  local url=split(picturePath,",")
  local pictureSize=table.getn(url)
  local listView=page:getElementById("picture_listID")
  listView:getEntity():getTemplateData():clear()
  local entityData={}
  for i=1,pictureSize,1 do
    if i < pictureSize then
      entityData[i]={subEntityValues={picture_typeID="looking",pictureID="local://"..url[i]}}
    elseif i == pictureSize and pictureSize < 6 then
      entityData[i]={subEntityValues={picture_typeID="photograph",pictureID="res://icon_picture_add.png"}}
    end
  end
  listView:upDateEntity(entityData)
end

--查看大图
function onBigImage(page)
  local path=split(picturePath,",")
  local pathSize=table.getn(path)
  local listView=page:getElementById("look_imageID")
  listView:getEntity():getTemplateData():clear()
  local entityData={}
  for i=1,pathSize-1,1 do
    entityData[i]={subEntityValues={big_imageID="local://"..path[i]}}
  end
  listView:upDateEntity(entityData)
  imagePopupView:showInFragment("center",0,0)
end

function onPictureDismiss(page,this)
  imagePopupView:dismiss()
end

--提交
function onPictureSubmit(page)
  local signaturePath=page:getArguments():getString("signaturePath")
  if signaturePath == "" or signaturePath == nil then
    page:showToast("请先签名")
    return
  end
  setElementEnable(page,"btn_submitID",true)
  
  local path=split(picturePath,",")
  local size=table.getn(path)
  local postFilePaths=getImagePostFilePaths(page,signaturePath,path,size)
  local remark=getElementValue(page,"remarkID")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/imgsUpload.do",
    LuaNetCallBack="PictureSubmitNetCallBack(page,{response})",
    LuaExceptionCallBack="PictureSubmitExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,businessType="流量年包",remark=remark},
    PostFilePaths=postFilePaths,
    Method="FORM",
    ResponseType="JSON",    
    Message="正在提交"
  }
  page:request(Parameters);
end

function PictureSubmitNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    onBusinessHandle(page)
  else
    setElementEnable(page,"btn_submitID",false)
    page:showToast("图片上传失败")
  end
end

function PictureSubmitExceptionCallBack(page)
  setElementEnable(page,"btn_submitID",false)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--办理业务
function onBusinessHandle(page)
  local remark=getElementValue(page,"remarkID")
  
  page:getActivity():showProgreesDialog("正在办理")
  local Parameters={
    NetSelector="NetData:",                
    URL="/fluxPackage/fluxHandleOrder.do",
    LuaNetCallBack="BusinessHandleNetCallBack(page,{response})",
    LuaExceptionCallBack="BusinessHandleExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",IP="",commodityCode=planID,totalPrice=planPrice,payMode="",commodityName=planName,isDistribution="",protocolType="",recommendNumber="",memo=remark,operatorid="",consigneeName="",consigneeMobileNo="",consigneeTel="",address="",postalCode="",deliveryTime="",certType="",certNum="",custName="",certAddr="",linkPhone="",custAddress="",postCode="",isRecommend=isRecom,cospCode=commodityId},
    Method="POST",
    ResponseType="JSON",    
--    Message="正在办理"
  }
  page:request(Parameters);
end

function BusinessHandleNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  page:getActivity():hideProgreesDialog()
  if response["resultCode"] == 0 then
    setElementHidden(page,"group_apply2ID",true)
    setElementHidden(page,"group_passID",false)
    --倒计时
    onCountDown(page,"btn_flowYearID")
  else
    setElementValue(page,"fail_wordID",response["resultMsg"])
    setElementHidden(page,"group_apply2ID",true)
    setElementHidden(page,"group_failID",false)
  end
end

function BusinessHandleExceptionCallBack(page)
  setElementEnable(page,"btn_submitID",false)
  page:showToast("网络异常！")
  page:getActivity():hideProgreesDialog()
end


-----^_^.^_^.^_^.^_^.^_^-------------成功or失败-------------^_^.^_^.^_^.^_^.^_^-----

--本次服务已完成
function onCustomerQuit(page)
  onCustomerLoginQuit(page)
end

--重新进行
function onAgain(page)
  setElementEnable(page,"btn_submitID",false)
  setElementHidden(page,"group_apply1ID",false)
  setElementHidden(page,"group_failID",true)
end

function goBack(page)
  page:setCustomAnimations("right")
  page:goBack()  
end

function onBackPressed(page)
  goBack(page)
end