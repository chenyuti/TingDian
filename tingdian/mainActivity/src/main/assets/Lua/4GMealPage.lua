require ("public")
local userInfoSP
local imagePopupView
local mobileNum --客户电话号码
local mealBusinessID --办理套餐对应的套餐ID
local mealType --办理套餐类型，fly飞享套餐，flow流量套餐，overlying流量叠加包
local mealName --办理套餐的名字
local picturePath=""

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  imagePopupView=page:getElementById("image_popupID")
  
  customerDetail(page)
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


-----^_^.^_^.^_^.^_^.^_^-------------小左边-------------^_^.^_^.^_^.^_^.^_^-----

function customerDetail(page)
  mealBusinessID=page:getArguments():getString("mealBusinessID")
  mealType=page:getArguments():getString("mealType")
  mealName=page:getArguments():getString("mealName")  
  mobileNum=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
  local customerBrand=page:getActivity():decrypt(userInfoSP:getString("brand",""))
  local customerMenu=page:getActivity():decrypt(userInfoSP:getString("menu",""))
  
  setElementValue(page,"customer_mobileID",mobileNum)
  setElementValue(page,"customer_brandID",customerBrand)
  setElementValue(page,"customer_mealID",customerMenu)
  
  if mealType == "fly" then
    setElementValue(page,"meal_typeID","已选飞享套餐档次：")
  elseif mealType == "flow" then
    setElementValue(page,"meal_typeID","已选手机流量套餐：")
  elseif mealType == "overlying" then
    setElementValue(page,"meal_typeID","已选流量叠加包：")
  elseif mealType == "king" then
    setElementValue(page,"meal_typeID","已选流量王套餐：")
  end
  setElementValue(page,"btn_mealID",mealName)
end


-----^_^.^_^.^_^.^_^.^_^-------------步骤-------------^_^.^_^.^_^.^_^.^_^-----

--业务单详情
function onProtocol(page)
  local managers=page:getActivity():decrypt(userInfoSP:getString("managers",""))
  local signatureURL=getElementValue(page,"signature_urlID")
  local bundle=page:initBundle()
    bundle:putString("protocol","4G")
    bundle:putString("signatureURL",signatureURL)
    if mealType == "fly" then
      bundle:putString("businessType","fourG")
      bundle:putString("discount","0")
      bundle:putString("actual","0")
      bundle:putString("commodityCode",string.sub(mealName,0,string.len(mealName)-3))
    elseif mealType == "flow" then
      local discount=page:getArguments():getString("discount")  
      local actual=page:getArguments():getString("actual")  
      bundle:putString("businessType","fluxPackage")
      bundle:putString("discount",""..discount/100)
      bundle:putString("actual",""..actual/100)
      bundle:putString("commodityCode",mealBusinessID)
    elseif mealType == "overlying" then
      local discount=page:getArguments():getString("discount")  
      local actual=page:getArguments():getString("actual")  
      bundle:putString("businessType","overlayPackage")
      bundle:putString("discount",""..discount/100)
      bundle:putString("actual",""..actual/100)
      bundle:putString("commodityCode",mealBusinessID)
    elseif mealType == "king" then
      bundle:putString("businessType","fluxKing")
      bundle:putString("discount","0")
      bundle:putString("actual","0")
      bundle:putString("commodityCode","")
    end
    page:setCustomAnimations("left")
    if managers == "true" then
      page:nextPage("res://protocolPage.xml",false,bundle) 
    elseif managers == "false" then
      page:nextPage("res://managersPage.xml",false,bundle) 
    end    
end

function onPageResume(page)
  local signaturePath=page:getArguments():getString("signaturePath")
  if signaturePath ~= getElementValue(page,"signature_urlID") and signaturePath ~= nil then
    setElementValue(page,"signature_urlID",signaturePath)
    setElementValue(page,"signatureID","local://"..signaturePath)
  end
end

--拍照
function onPicture(page,this)
  local pictureType=this:getElementById("picture_typeID"):getValue()
  if pictureType == "photograph" then
    if mealType == "fly" then
      page:getActivity():photograph(mobileNum,"FourGHandle")
    elseif mealType == "flow" then
      page:getActivity():photograph(mobileNum,"FluxPackageHandle")
    elseif mealType == "overlying" then
      page:getActivity():photograph(mobileNum,"OverlayHandle")
    elseif mealType == "king" then
      page:getActivity():photograph(mobileNum,"FluxKingHandle")
    end  
  elseif pictureType == "looking" then
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
local businessType
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
  
  if mealType == "fly" then
    businessType="4G飞享套餐"
  elseif mealType == "flow" then
    businessType="手机流量套餐"
  elseif mealType == "overlying" then
    businessType="流量叠加包"
  elseif mealType == "king" then
    businessType="流量王套餐"
  end
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/imgsUpload.do",
    LuaNetCallBack="PictureSubmitNetCallBack(page,{response})",
    LuaExceptionCallBack="PictureSubmitExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,businessType=businessType,remark=remark},
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
    if mealType == "fly" or mealType == "king" then
      onFlyKing(page)
    elseif mealType == "flow" or mealType == "overlying" then
      onFlowOverlying(page)
    end
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

--飞享套餐
local orderTypeName
local commodityName
function onFlyKing(page)
  local totalPrice=string.sub(mealName,0,string.len(mealName)-3)
  if mealType == "fly" then
    orderTypeName="4G飞享套餐"
    commodityName=mealType.."4G飞享套餐"
  elseif mealType == "king" then
    orderTypeName="流量王套餐"
    commodityName=mealType.."流量王套餐"
  end
  
  page:getActivity():showProgreesDialog("正在办理")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/handleOrder.do",
    LuaNetCallBack="onMealNetCallBack(page,{response})",
    LuaExceptionCallBack="onMealExceptionCallBack(page)",
    RequestParams={orderType=orderTypeName,phoneNum=mobileNum,channel="",IP="",commodityCode=mealBusinessID,totalPrice=totalPrice,payMode="",commodityName=commodityName,isDistribution="",protocolType="",recommendNumber="",memo="",operatorid="",consigneeName="",consigneeMobileNo="",consigneeTel="",address="",postalCode="",deliveryTime="",certType="",certNum="",custName="",certAddr="",linkPhone="",custAddress="",postCode=""},
    Method="POST",
    ResponseType="JSON",    
--    Message="正在办理"
  }
  page:request(Parameters);
end

--手机流量套餐or流量叠加包
function onFlowOverlying(page)
  local orderNum=page:getArguments():getString("orderNum")  
  local actual=page:getArguments():getString("actual")  
  page:getActivity():showProgreesDialog("正在办理")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/handleCommitOrder.do",
    LuaNetCallBack="onMealNetCallBack(page,{response})",
    LuaExceptionCallBack="onMealExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",orderSeq=orderNum,payType="1",fee=actual,payMode="",payOrg="",paySeq="",custId="",custType="1"},
    Method="POST",
    ResponseType="JSON",    
--    Message="正在办理"
  }
  page:request(Parameters);
end

function onMealNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  page:getActivity():hideProgreesDialog()
  if response["resultCode"] == 0 then
    setElementHidden(page,"group_applyID",true)
    setElementHidden(page,"group_passID",false)
    --倒计时
    onCountDown(page,"btn_4GMealID")
  else
    setElementValue(page,"fail_wordID",response["resultMsg"])
    setElementHidden(page,"group_applyID",true)
    setElementHidden(page,"group_failID",false)
  end
end

function onMealExceptionCallBack(page)
  setElementEnable(page,"btn_submitID",false)
  page:showToast("网络异常！")
  page:getActivity():hideProgreesDialog()
end


-----^_^.^_^.^_^.^_^.^_^-------------成功or失败-------------^_^.^_^.^_^.^_^.^_^-----

--本次服务已完成
function onCustomerQuit(page)
  onCustomerLoginQuit(page)
end

function onAgain(page)
  setElementEnable(page,"btn_submitID",false)
  setElementHidden(page,"group_applyID",false)
  setElementHidden(page,"group_failID",true)
end

function goBack(page)
  page:setCustomAnimations("right")
  page:goBack()  
end

function onBackPressed(page)
  goBack(page)
end