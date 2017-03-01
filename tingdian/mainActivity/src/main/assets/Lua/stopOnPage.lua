require ("public")
local userInfoSP
local serviceButton --服务密码按钮
local loginSelected --服务密码=service，本人凭证件=IDCard，有效证件=valid
local imagePopupView --查看大图的PopopView

local mobileNum --客户电话号码
local stopButton
local sType --1为申请停机，6为申请开机
local picturePath=""


function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  serviceButton=page:getElementById("btn_serviceID")
  stopButton=page:getElementById("btn_stopID")
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
  mobileNum=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
  local customerLoginType=page:getActivity():decrypt(userInfoSP:getString("loginType",""))
  local customerBrand=page:getActivity():decrypt(userInfoSP:getString("brand",""))
  local customerStopOn=page:getActivity():decrypt(userInfoSP:getString("stopOn",""))
  
  setElementValue(page,"customer_mobileID",mobileNum)
  setElementValue(page,"customer_brandID",customerBrand)
  if customerStopOn == "0" then
    setElementValue(page,"customer_stopOnID","已停机")
    setElementHidden(page,"group_openID",false)
    setElementHidden(page,"group_stopID",true)
    onSelectedStop(page,stopButton,"6")
  elseif customerStopOn == "1" then
    setElementValue(page,"customer_stopOnID","正使用")
    setElementHidden(page,"group_openID",true)
    setElementHidden(page,"group_stopID",false)
    sType="1"
  end
end

--服务密码、本人凭证件、有效证件选择
local nextLogin
function onLoginSelected(page,this,type)
  if nextLogin ~= nil and nextLogin ~= this then
    nextLogin:setSelected(false)
  end
  this:setSelected(true)
  nextLogin=this
  
  setElementHidden(page,"group_serviceID",true)
  setElementHidden(page,"group_idcardID",true)
  setElementHidden(page,"group_validID",true)
  if type == "service" then
    setElementHidden(page,"group_serviceID",false)
  elseif type == "IDCard" then
    setElementHidden(page,"group_idcardID",false)
  elseif type == "valid" then
    setElementHidden(page,"group_validID",false)
  end
  loginSelected=type
end

function onValueChange(page,this)
  local selectedDocument=getElementValue(page,"selected_validID")
  if selectedDocument == "身份证" then
    setElementHidden(page,"group_validIDCardID",false)
    setElementHidden(page,"group_validOtherID",true)
  else
    setElementHidden(page,"group_validIDCardID",true)
    setElementHidden(page,"group_validOtherID",false)
  end
end

--身份证登录鉴权
local scanningCard
function onBluetoothScanning(page,type)
  local bluetoothDevice=page:getActivity():decrypt(userInfoSP:getString("bluetoothDevice",""))
  local deviceConnect=page:getActivity():decrypt(userInfoSP:getString("deviceConnect",""))
  if bluetoothDevice == "" then
    page:setCustomAnimations("left")
    page:nextPage("res://devicePage.xml",false,nil)
  return  
  end

  scanningCard=type
  if deviceConnect == "true" then
    page:getActivity():readCard()
  elseif deviceConnect == "false" then
    page:getActivity():showProgreesDialog("正在连接证件扫描仪")
    page:getActivity():sleep(page,nil,"onScanning(page)",500)  
  end
end

function onScanning(page)
  local bluetoothDevice=page:getActivity():decrypt(userInfoSP:getString("bluetoothDevice",""))
  page:getActivity():connectScanning(bluetoothDevice)
  local deviceConnect=page:getActivity():decrypt(userInfoSP:getString("deviceConnect",""))
  if deviceConnect == "true" then
    page:getActivity():readCard()
  end
end

function ScanningResult(page)
  local cardDetail=page:getActivity():getCard()
  local cardItem=LuaRemove(cardDetail," ")
  local card=split(cardItem,",")
  if scanningCard == "idcard" then
    setElementValue(page,"idcard_numID","")
    setElementValue(page,"idcard_numID",card[3])
  elseif scanningCard == "valid" then
    page:getElementById("valid_idcardNumID"):clearText()
    setElementValue(page,"valid_idcardNumID",card[3])
  end
end

--鉴权确认
function onLoginSure(page)
  if loginSelected == "service" then
    local serviceNum=getElementValue(page,"service_numID")
    if serviceNum == "" or serviceNum == nil then
      page:showToast("请输入服务密码")
    return
    end   
  
    local Parameters={
      NetSelector="NetData:",                
      URL="/login/pwdLogin.do",
      LuaNetCallBack="LoginNetCallBack(page,{response})",
      LuaExceptionCallBack="LoginExceptionCallBack(page)",
      RequestParams={phoneNum=mobileNum,pwd=serviceNum,channel=""},
      Method="POST",
      ResponseType="JSON",    
      Message="正在确认"
    }
    page:request(Parameters);
  elseif loginSelected == "IDCard" then
    local numValue=getElementValue(page,"idcard_numID")
    if numValue == "" or numValue == nil then
      page:showToast("请读取证件号码")
    return
    end
       
    local Parameters={
      NetSelector="NetData:",                
      URL="/login/CardsLogin.do",
      LuaNetCallBack="LoginNetCallBack(page,{response})",
      LuaExceptionCallBack="LoginExceptionCallBack(page)",
      RequestParams={phoneNum=mobileNum,channel="",custType="1",verType="AuthCheckE",typeValue="IdCard",codeValue=numValue},
      Method="POST",
      ResponseType="JSON",    
      Message="正在确认"
    }
    page:request(Parameters);
  elseif loginSelected == "valid" then
    local validNum
    local selectedValid=getElementValue(page,"selected_validID")
    if selectedValid == "身份证" then
      validNum=getElementValue(page,"valid_idcardNumID")
      if validNum == "" or validNum == nil then
        page:showToast("请读取证件号码")
        return
      end 
    else
      validNum=getElementValue(page,"valid_otherNumID")
      if validNum == "" or validNum == nil then
        page:showToast("请输入证件号码")
        return
      end 
    end     
    
    local typeValue=validSelectedValue(page,selectedValid)
    local verType
    if selectedValid == "身份证" then
      verType="AuthCheckE"
    else
      verType="AuthCheckD"
    end    
    local Parameters={
      NetSelector="NetData:",                
      URL="/login/CardsLogin.do",
      LuaNetCallBack="LoginNetCallBack(page,{response})",
      LuaExceptionCallBack="LoginExceptionCallBack(page)",
      RequestParams={phoneNum=mobileNum,channel="",custType="1",verType=verType,typeValue=typeValue,codeValue=validNum},
      Method="POST",
      ResponseType="JSON",    
      Message="正在确认"
    }
    page:request(Parameters);
  end  
end

function LoginNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    setElementHidden(page,"group_loginID",true)
    setElementHidden(page,"group_notLoginID",true)
    local edit=userInfoSP:edit()
      edit:putString("loginType",page:getActivity():encrypt(loginSelected))
      edit:commit()    
  else
    page:showToast(response["resultMsg"])
  end
end

function LoginExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function validSelectedValue(page,value)
  local validValueID
  if value == "身份证" then
    validValueID="IdCard"
  elseif value == "士兵证" then
    validValueID="SoldierID"
  elseif value == "警官证" then
    validValueID="PolicePaper"
  elseif value == "护照" then
    validValueID="Passport"
  elseif value == "户口簿" then
    validValueID="HuKouBu"
  elseif value == "港澳证" then
    validValueID="HKIdCard"
  elseif value == "VIP卡" then
    validValueID="IdVipCard"
  elseif value == "公章" then
    validValueID="GongZhang"
  elseif value == "临时身份证" then
    validValueID="TempId"
  elseif value == "驾驶证" then
    validValueID="DriverIC"
  elseif value == "军官证" then
    validValueID="PLA"
  elseif value == "单位介绍信" then
    validValueID="IdTypeJSX"
  elseif value == "学生证" then
    validValueID="StudentID"
  elseif value == "教师证" then
    validValueID="TeacherID"
  elseif value == "复员证" then
    validValueID="FuYuanZheng"
  elseif value == "暂住证" then
    validValueID="ZanZhuZheng"
  elseif value == "还乡证" then
    validValueID="RetNative"
  elseif value == "其他" then
    validValueID="OtherLicence"
  elseif value == "营业执照" then
    validValueID="BusinessLicence"
  elseif value == "事业单位法人证书" then
    validValueID="CorpLicence"
  elseif value == "单位证明" then
    validValueID="UnitID"
  elseif value == "社会团体法人登记证书" then
    validValueID="OrgaLicence"    
  end
  return validValueID  
end


-----^_^.^_^.^_^.^_^.^_^-------------申请停开机步骤-------------^_^.^_^.^_^.^_^.^_^-----

local nextStop
function onSelectedStop(page,this,type)
  if nextStop ~= nil and nextStop ~= this then
    nextStop:setSelected(false)
  end
  this:setSelected(true)
  nextStop=this
  
  sType=type
end

--业务单详情
function onProtocol(page)
  local signatureURL=getElementValue(page,"signature_urlID")
  local bundle=page:initBundle()
    bundle:putString("protocol","stopOn")
    bundle:putString("signatureURL",signatureURL)
    if sType == "1" then
      bundle:putString("businessType","stopMachine")
    elseif sType == "6" or sType == "7" then
      bundle:putString("businessType","startMachine")
    end
    page:setCustomAnimations("left")
    page:nextPage("res://protocolPage.xml",false,bundle) 
end

--打印
function onPrint(page)
  local signaturePath=page:getArguments():getString("signaturePath")
  local protocolDetail=page:getArguments():getString("protocolDetail")
  local protocolURL=page:getActivity():getProtocol(signaturePath,protocolDetail)
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/print/printImgUpload.do",
    LuaNetCallBack="onPrintNetCallBack(page,{response})",
    LuaExceptionCallBack="onPrintExceptionCallBack(page)",
    RequestParams={},
    PostFilePaths={{"pics1",protocolURL}},
    Method="FORM",
    ResponseType="JSON",    
    Message="正在提交打印"
  }
  page:request(Parameters);  
end

function onPrintNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    page:showToast("已成功提交打印")
  else
    page:showToast("提交打印失败")
  end
end

function onPrintExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end 

function onPageResume(page)
  local signaturePath=page:getArguments():getString("signaturePath")
  if signaturePath ~= getElementValue(page,"signature_urlID") and signaturePath ~= nil then
    setElementValue(page,"signature_urlID",signaturePath)
    setElementValue(page,"signatureID","local://"..signaturePath)
    setElementEnable(page,"btn_printID",false)
  end
end

--拍照
function onPicture(page,this)
  local pictureType=this:getElementById("picture_typeID"):getValue()
  if pictureType == "photograph" then
    if sType == "1" then
      page:getActivity():photograph(mobileNum,"StopMachine")
    elseif sType == "6" or sType == "7" then
      page:getActivity():photograph(mobileNum,"OpenMachine")
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
    RequestParams={phoneNum=mobileNum,businessType="停开机",remark=remark},
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
    URL="/client/serviceModify.do",
    LuaNetCallBack="BusinessHandleNetCallBack(page,{response})",
    LuaExceptionCallBack="BusinessHandleExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",IP="",sType=sType,remark=remark},
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
    setElementHidden(page,"group_applyID",true)
    setElementHidden(page,"group_passID",false)
    if sType == "1" then
      setElementValue(page,"pass_wordID","您的号码已停用！")
      setElementValue(page,"customer_stopOnID","已停机")
      local edit=userInfoSP:edit()
      edit:putString("stopOn",page:getActivity():encrypt("0"))
      edit:commit()
    elseif sType == "6" or sType == "7" then
      setElementValue(page,"pass_wordID","您的号码已启用！")
      setElementValue(page,"customer_stopOnID","正使用")
      local edit=userInfoSP:edit()
      edit:putString("stopOn",page:getActivity():encrypt("1"))
      edit:commit()
    end
  --倒计时
    onCountDown(page,"btn_timingID")
  else
    setElementValue(page,"fail_wordID",response["resultMsg"])
    setElementHidden(page,"group_applyID",true)
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

