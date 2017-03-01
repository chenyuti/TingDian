require ("public")
local userInfoSP
local errorPopupView
local imagePopupView
local dealButton
local posButton
local mobileNum
local selectedAddress="" --办理家宽-选择的地址
local planOldID
local planID --选择的宽带产品ID
local speedValue --选择的宽带产品速度
local freeValue --选择的宽带产品是否免费
local moneyValue --选择的宽带产品价格
local planName --选择的宽带产品名称
local tipsView --温馨提示
local picturePath=""
local isRecom
local commodityId

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  errorPopupView=page:getElementById("pop_errorID")
  imagePopupView=page:getElementById("image_popupID")
  dealButton=page:getElementById("btn_dealID")
  posButton=page:getElementById("btn_posID")
  tipsView=page:getElementById("tips_popupID")
  isRecom=page:getArguments():getString("isRecommend")
  commodityId=page:getArguments():getString("commodityId")
  
  mobileNum=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
  if mobileNum == "" then
    setElementHidden(page,"group_loginID",true)
    setElementHidden(page,"group_applyID",false)
    onStep(page,dealButton,"deal")
    page:getActivity():sleep(page,nil,"onTipsShow(page)",500)  
  else
    setElementHidden(page,"group_loginID",false)    
    customerDetail(page)
  end
end

function onTipsShow(page)
  tipsView:showInFragment("center",0,0)
end

function onTips(page)
  tipsView:dismiss()
end

function onPageResume(page)
  local signaturePath=page:getArguments():getString("signaturePath")
  if signaturePath ~= getElementValue(page,"signature_urlID") and signaturePath ~= nil then
    setElementValue(page,"signature_urlID",signaturePath)
    setElementValue(page,"signatureID","local://"..signaturePath)
  end
  
  local newAddress=page:getArguments():getString("newAddress")
  if newAddress ~= nil then
    selectedAddress=newAddress
  end
end


-----^_^.^_^.^_^.^_^.^_^-------------小左边-------------^_^.^_^.^_^.^_^.^_^-----

function customerDetail(page)
  local customerBrand=page:getActivity():decrypt(userInfoSP:getString("brand",""))
  local customerBroadband=page:getActivity():decrypt(userInfoSP:getString("broadband",""))
  local customerBroadbandEnd=page:getActivity():decrypt(userInfoSP:getString("broadbandEnd",""))
  local dealEntrance=page:getArguments():getString("dealEntrance")
  
  setElementValue(page,"customer_mobileID",mobileNum)
  setElementValue(page,"customer_brandID",customerBrand)
  
  if customerBroadband == "yes" then
    setElementValue(page,"customer_broadbandID","家宽用户")
    if dealEntrance == "service" then
      onQualifiedCheck(page)
    else
      onQualifiedCheck(page)
      setElementHidden(page,"group_dueID",false)
      setElementValue(page,"due_timeID",customerBroadbandEnd)
      setElementHidden(page,"group_apply2ID",false)
    end
  elseif customerBroadband == "no" then
    setElementValue(page,"customer_broadbandID","非家宽用户")
    setElementHidden(page,"group_dueID",true)
    if dealEntrance == "cover" then
      onQualifiedCheck(page)
    else
      setElementHidden(page,"group_applyID",false)
    end
  end
  
  onStep(page,dealButton,"deal")
  page:getActivity():sleep(page,nil,"onTipsShow(page)",500)  
end

local nextStep
function onStep(page,this,type)
  if nextStep ~= nil and nextStep ~= this then
    nextStep:setSelected(false)
  end
  nextStep=this
  this:setSelected(true)
  
  setElementHidden(page,"group_dealID",true)
  setElementHidden(page,"group_apply4ID",true)
  if type == "deal" then
    setElementHidden(page,"group_dealID",false)
  elseif type == "schedule" then
    setElementHidden(page,"group_apply4ID",false)
--    onSchedule(page)
  end
end

--覆盖查询
function onCoverQuery(page)
  local bundle=page:initBundle()
    bundle:putString("entrance","broadband")  
    page:setCustomAnimations("left")
    page:nextPage("res://coverQueryPage.xml",false,bundle)
end


-----^_^.^_^.^_^.^_^.^_^-------------步骤1-------------^_^.^_^.^_^.^_^.^_^-----

local areaWord="福田区"
function onValueChange(page,this)
  local areaValue=getElementValue(page,"areaID")
  local searchValue=getElementValue(page,"searchID")
  if areaValue ~= areaWord and searchValue ~= "" then
    areaWord=areaValue
    onAddressList(page,areaValue,searchValue)
  end
end

local searchWord=""
function onTextChanged(page,this)
  local areaValue=getElementValue(page,"areaID")
  local searchValue=getElementValue(page,"searchID")
  if searchValue ~= searchWord then
    areaWord=areaValue
    searchWord=searchValue
    if searchValue == "" then
      setElementHidden(page,"address_listID",true)
    elseif searchValue ~= "" then
      setElementEnable(page,"btn_nowDealID",true)
      page:getActivity():sleep(page,nil,"onSearchList(page,"..searchValue..")",500)  
    end      
  elseif searchValue == searchWord and searchWord ~= "" then
    setElementEnable(page,"btn_nowDealID",false)
  end
end

function onSearchList(page,word)
  local areaValue=getElementValue(page,"areaID")
  local searchValue=getElementValue(page,"searchID")
  if searchValue == word then
    onAddressList(page,areaValue,searchValue)  
  end
end

function onAddressList(page,areaValue,searchValue)
  local Parameters={
    NetSelector="NetData:",                
    URL="/broadband/addressSearch.do",
    LuaNetCallBack="AddressListNetCallBack(page,{response})",
    LuaExceptionCallBack="AddressListExceptionCallBack(page)",
    RequestParams={district=areaValue,detailedAddr=searchValue},
    Method="POST",
    ResponseType="JSON",    
--    Message="正在加载"
  }
  page:request(Parameters);
end

function AddressListNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]["AddrList"]
    if #data == 0 then
      setElementHidden(page,"address_listID",true)
    else
      setElementHidden(page,"address_listID",false)
      local listView=page:getElementById("address_listID")
      listView:getEntity():clearTemplateData()
      local entityData={}
        for i=1,#data,1 do
          entityData[i]={subEntityValues={address_nameID=data[i]}}
        end  
      listView:upDateEntity(entityData)    
    end
  else
    page:showToast(response["resultMsg"])
  end
end

function AddressListExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function onAddressSelected(page,this)
  selectedAddress=this:getElementById("address_nameID"):getValue()
  searchWord=selectedAddress
  setElementValue(page,"searchID",selectedAddress)
  setElementHidden(page,"address_listID",true)
end

--马上办理
function onNowDeal(page)
  if mobileNum == "" then
    local bundle=page:initBundle()
      bundle:putString("broadbandEntrance","broadband")
      page:setCustomAnimations("right")
      page:goBack(bundle)  
      page:showToast("请先登录")
  return
  end
  
  onQualifiedCheck(page)
end

--资格校验
function onQualifiedCheck(page)  
  local Parameters={
    NetSelector="NetData:",                
    URL="/broadband/broadbandQualified.do",
    LuaNetCallBack="QualifiedCheckNetCallBack(page,{response})",
    LuaExceptionCallBack="QualifiedCheckExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum},
    Method="POST",
    ResponseType="JSON",    
    Message="正在校验"
  }
  page:request(Parameters);
end

local menuRange
function QualifiedCheckNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    menuRange=response["response"]["data"]["SalePrice"]
    local dealEntrance=page:getArguments():getString("dealEntrance")
    if dealEntrance == "service" then
      planOldID=page:getArguments():getString("broadbandOldID")
      planID=page:getArguments():getString("broadbandID")
      speedValue=page:getArguments():getString("broadbandSpeed")
      freeValue=page:getArguments():getString("broadbandFree")
      moneyValue=page:getArguments():getString("broadbandMoney")
      planName=page:getArguments():getString("broadbandName")
      if string.find(menuRange,"client"..speedValue) == nil then
        page:showToast("你的4G套餐类型不符合办理条件")
        goBack(page)
      return
      end
      
      --是否已身份确认，是进行一证五户校验，不是直接到办理
      local customerCardType=page:getActivity():decrypt(userInfoSP:getString("cardType",""))
      if customerCardType == "" then
        setElementValue(page,"price_ID","￥"..moneyValue)
        
        if freeValue == "0" then
          setElementHidden(page,"group_freeID",true)
        elseif freeValue == "1" then
          setElementHidden(page,"group_freeID",false)
          onPaymentSelected(page,posButton,"pos")
        end
        onStartImageResult(page)
          
        setElementHidden(page,"group_applyID",true)
        setElementHidden(page,"group_apply3ID",false)
      else
        onCheckDecoment(page)
      end            

    else
      onBroadbandList(page)
    end
  else
    setElementValue(page,"fail_wordID",response["resultMsg"])
    setElementHidden(page,"group_applyID",true)
    setElementHidden(page,"group_failID",false)
  end
end

function QualifiedCheckExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

local speedAll=""
local termAll=""
local planData
function onBroadbandList(page)
  page:getActivity():showProgreesDialog("正在加载")
  local Parameters={
    NetSelector="NetData:",                
    URL="/broadband/openBroadband2.do",
    LuaNetCallBack="BroadbandListNetCallBack(page,{response})",
    LuaExceptionCallBack="BroadbandListExceptionCallBack(page)",
    RequestParams={},
    Method="POST",
    ResponseType="JSON",    
--    Message="正在加载"
  }
  page:request(Parameters);
end

function BroadbandListNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  page:getActivity():hideProgreesDialog()
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    planData=data
    for i=1,#data,1 do
      speedAll=speedAll..data[i]["Speedtype"]..","
      termAll=termAll..data[i]["PromiseMonth"]..","
    end
    
    onSpeedList(page)
    onTermList(page)
    onPlanList(page)
    
    setElementHidden(page,"group_applyID",true)
    setElementHidden(page,"group_apply2ID",false)
  else
    page:showToast(response["resultMsg"])
  end
end

function BroadbandListExceptionCallBack(page)
  page:showToast("网络异常！")
  page:getActivity():hideProgreesDialog()
--  page:hideProgreesDialog()
end

function onSpeedList(page)
  local speedList=page:getActivity():getSpinnerList(speedAll)
  local speedList1=LuaRemove(speedList," ")
  local speedList2=string.sub(speedList1,2,string.len(speedList1)-1)
  local speed=split(speedList2,",")
  
  local speedView=page:getElementById("speed_listID")
  speedView:getEntity():clearTemplateData()
  local entityData={}
  for i=1,#speed,1 do
    entityData[i]={subEntityValues={speed_nameID=speed[i].."M"}}
  end
  speedView:upDateEntity(entityData)
end

function onTermList(page)
  local termList=page:getActivity():getSpinnerList(termAll)
  local termList1=LuaRemove(termList," ")
  local termList2=string.sub(termList1,2,string.len(termList1)-1)
  local term=split(termList2,",")
    
  local termView=page:getElementById("term_listID")
  termView:getEntity():clearTemplateData()
  local entityData={}
  for i=1,#term,1 do
    entityData[i]={subEntityValues={term_nameID=term[i].."个月"}}
  end
  termView:upDateEntity(entityData)
end

function onPlanList(page)
  local listView=page:getElementById("plan_listID")
  listView:getEntity():clearTemplateData()
  local entityData={}
  for i=1,#planData,1 do
    entityData[i]={subEntityValues={plan_oldID=planData[i]["PackageMode"],plan_ID=planData[i]["CospCode"],speed_ID=planData[i]["Speedtype"],free_ID=planData[i]["NeedPay"],money_ID=planData[i]["Amount"],plan_nameID=planData[i]["Instruction"]},attributes={selected="false"}}
  end
  listView:upDateEntity(entityData) 
end


-----^_^.^_^.^_^.^_^.^_^-------------步骤2-------------^_^.^_^.^_^.^_^.^_^-----

--宽带选择
local speedSelected
local nextSpeed
function onSpeedSelected(page,this)
  if nextSpeed ~= nil and nextSpeed ~= this then
    nextSpeed:setSelected(false)
  end
  nextSpeed=this
  this:setSelected(true)
  
  if this:getElementById("speed_nameID"):getValue() ~= speedSelected then
    planID=""
    speedSelected=this:getElementById("speed_nameID"):getValue()
    onScreenList(page)
  end
end

--期限选择
local termSelected
local nextTerm
function onTermSelected(page,this)
  if nextTerm ~= nil and nextTerm ~= this then
    nextTerm:setSelected(false)
  end
  nextTerm=this
  this:setSelected(true)
  
  if this:getElementById("term_nameID"):getValue() ~= termSelected then
    planID=""
    termSelected=this:getElementById("term_nameID"):getValue()
    onScreenList(page)
  end
end

--方案选择
local nextPlan
function onPlanSelected(page,this)
  if nextPlan ~= nil and nextPlan ~= this then
    nextPlan:setSelected(false)
  end
  nextPlan=this
  this:setSelected(true)
  
  planOldID=this:getElementById("plan_oldID"):getValue()
  planID=this:getElementById("plan_ID"):getValue()
  speedValue=this:getElementById("speed_ID"):getValue()
  freeValue=this:getElementById("free_ID"):getValue()
  moneyValue=this:getElementById("money_ID"):getValue()
  planName=this:getElementById("plan_nameID"):getValue()
end

function onScreenList(page)
  local listView=page:getElementById("plan_listID")
  listView:getEntity():clearTemplateData()
  local entityData={}
  for i=1,#planData,1 do
    if (speedSelected == nil and planData[i]["PromiseMonth"].."个月" == termSelected) or (termSelected == nil and planData[i]["Speedtype"].."M" == speedSelected) or (planData[i]["Speedtype"].."M" == speedSelected and planData[i]["PromiseMonth"].."个月" == termSelected) then
      entityData[i]={subEntityValues={plan_oldID=planData[i]["PackageMode"],plan_ID=planData[i]["CospCode"],speed_ID=planData[i]["Speedtype"],free_ID=planData[i]["NeedPay"],money_ID=planData[i]["Amount"],plan_nameID=planData[i]["Instruction"]},attributes={selected="false"}}
    end
  end
  listView:upDateEntity(entityData) 
end

--办理4G套餐
function on4GMenu(page)
  page.manager:removeFragment("res://customerServicePage.xml")    
  local bundle=page:initBundle()
    bundle:putString("4GMeal","4GMeal")
    page:setCustomAnimations("right")
    page:goBack(bundle)
end

--办理    
function onDealWith(page)
  if planID == "" or planID == nil then
    page:showToast("请选择宽带方案")
  return
  end
  
  if string.find(menuRange,"client"..speedValue) == nil then
    page:showToast("你的4G套餐类型不符合办理条件")
  return
  end
  
  local customerBroadband=page:getActivity():decrypt(userInfoSP:getString("broadband",""))
  local customerBroadbandEnd=page:getActivity():decrypt(userInfoSP:getString("broadbandEnd",""))
  local date=os.date("%Y%m%d".."000000")
  if customerBroadband == "yes" and customerBroadbandEnd >= ""..(date+100000000) then
    setElementValue(page,"error_tipsID","很抱歉，您的号码暂时不符合续约条件")
    errorPopupView:showInFragment("center",0,0)
  return
  end
  
  --是否已身份确认，是进行一证五户校验，不是直接到办理
  local customerCardType=page:getActivity():decrypt(userInfoSP:getString("cardType",""))
  if customerCardType == "" then
    setElementValue(page,"price_ID","￥"..moneyValue)
    setElementHidden(page,"group_apply2ID",true)
    setElementHidden(page,"group_apply3ID",false)
    
    if freeValue == "0" then
      setElementHidden(page,"group_freeID",true)
    elseif freeValue == "1" then
      setElementHidden(page,"group_freeID",false)
      onPaymentSelected(page,posButton,"pos")
    end
    onStartImageResult(page)  
  else
    --onCheckDecoment(page)
    
    setElementValue(page,"price_ID","￥"..moneyValue)
    setElementHidden(page,"group_apply2ID",true)
    setElementHidden(page,"group_apply3ID",false)
    
    if freeValue == "0" then
      setElementHidden(page,"group_freeID",true)
    elseif freeValue == "1" then
      setElementHidden(page,"group_freeID",false)
      onPaymentSelected(page,posButton,"pos")
    end
    onStartImageResult(page)  
  end
end

--校验一证五户
function onCheckDecoment(page)
  local customerCardType=page:getActivity():decrypt(userInfoSP:getString("cardType",""))
  local customerUserNum=page:getActivity():decrypt(userInfoSP:getString("userNum",""))
  local Parameters={
    NetSelector="NetData:",                
    URL="/account/queryOneCertMulMachine.do",
    LuaNetCallBack="onCheckDecomentNetCallBack(page,{response})",
    LuaExceptionCallBack="onCheckDecomentExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",certId=customerUserNum,certType=customerCardType},
    Method="POST",
    ResponseType="JSON",     
    Message="正在校验"
  }
  page:request(Parameters);
end

function onCheckDecomentNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 or (response["resultCode"] == 1 and response["response"]["data"]["Count"] == "") then
    setElementValue(page,"price_ID","￥"..moneyValue)
    
    if freeValue == "0" then
      setElementHidden(page,"group_freeID",true)
    elseif freeValue == "1" then
      setElementHidden(page,"group_freeID",false)
      onPaymentSelected(page,posButton,"pos")
    end 
    onStartImageResult(page)
    
    local dealEntrance=page:getArguments():getString("dealEntrance")
    if dealEntrance == "service" then
      setElementHidden(page,"group_applyID",true)
    else
      setElementHidden(page,"group_apply2ID",true)
    end      
    setElementHidden(page,"group_apply3ID",false)
  else
    setElementValue(page,"error_tipsID",response["resultMsg"])
    errorPopupView:showInFragment("center",0,0)  
  end
end

function onCheckDecomentExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function onErrorSure(page)
  errorPopupView:dismiss()
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

    
-----^_^.^_^.^_^.^_^.^_^-------------步骤3-------------^_^.^_^.^_^.^_^.^_^-----

--选择POS机or现金
local nextPay
function onPaymentSelected(page,this,type)
  if nextPay ~= nil and nextPay ~= this then
    nextPay:setSelected(false)
  end
  nextPay=this
  this:setSelected(true)
  
  if type == "pos" then
    setElementHidden(page,"group_posID",false)
  elseif type == "cash" then
    setElementHidden(page,"group_posID",true)
  end
end

--业务单详情
function onProtocol(page)
  local managers=page:getActivity():decrypt(userInfoSP:getString("managers",""))
  local signatureURL=getElementValue(page,"signature_urlID")
  local customerBroadband=page:getActivity():decrypt(userInfoSP:getString("broadband",""))
  local bundle=page:initBundle()
    bundle:putString("protocol","broadband")
    bundle:putString("signatureURL",signatureURL)
    bundle:putString("discount","0.00")
    bundle:putString("actual",moneyValue)
    if customerBroadband == "yes" then
      bundle:putString("homeBroadbandType","2")
    elseif customerBroadband == "no" then
      bundle:putString("homeBroadbandType","1")
    end    
    bundle:putString("planID",planID)
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
    page:getActivity():photograph(mobileNum,"Broadband")
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
    RequestParams={phoneNum=mobileNum,businessType="家庭宽带",remark=remark},
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
  local homeBroadbandType
  local customerBroadband=page:getActivity():decrypt(userInfoSP:getString("broadband",""))
  if customerBroadband == "yes" then
    homeBroadbandType="2"
  elseif customerBroadband == "no" then
    homeBroadbandType="1"
  end
  
  page:getActivity():showProgreesDialog("正在办理")
  local Parameters={
    NetSelector="NetData:",                
    URL="/broadband/handleOrder.do",
    LuaNetCallBack="BusinessHandleNetCallBack(page,{response})",
    LuaExceptionCallBack="BusinessHandleExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",IP="",commodityCode=planID,totalPrice=moneyValue,payMode="",commodityName=planName,isDistribution="0",protocolType="2",recommendNumber="",memo="",operatorid="",consigneeName="",consigneeMobileNo="",consigneeTel="",address="",postalCode="",deliveryTime="",certType="",certNum="",custName="",certAddr="",linkPhone="",custAddress="",postCode="",cellNumber="",installedAddress=selectedAddress,homeBroadbandType=homeBroadbandType,isRecommend=isRecom,cospCode=commodityId},
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
    setElementHidden(page,"group_apply3ID",true)
    setElementHidden(page,"group_passID",false)
    --倒计时
    onCountDown(page,"btn_broadbandID")
  else
    setElementEnable(page,"btn_submitID",false)
    page:showToast(response["resultMsg"])
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
  setElementHidden(page,"group_applyID",false)
  setElementHidden(page,"group_failID",true)
end


-----^_^.^_^.^_^.^_^.^_^-------------进度查询-------------^_^.^_^.^_^.^_^.^_^-----

function onSchedule(page)
  local Parameters={
    NetSelector="NetData:",                
    URL="",
    LuaNetCallBack="ScheduleNetCallBack(page,{response})",
    LuaExceptionCallBack="ScheduleExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);

end

function ScheduleNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    
    local listView=page:getElementById("schedule_listID")
    listView:getEntity():clearTemplateData()
    local entityData={}
    for i=1,#planData,1 do
      entityData[i]={subEntityValues={schedule_orderNumID=data[i][""],schedule_typeID=data[i][""],schedule_remarkID=data[i][""]}}
    end
    listView:upDateEntity(entityData) 
  else
    page:showToast(response["resultMsg"])
  end
end

function ScheduleExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function goBack(page)
  page:setCustomAnimations("right")
  page:goBack()  
end

function onBackPressed(page)
  goBack(page)
end