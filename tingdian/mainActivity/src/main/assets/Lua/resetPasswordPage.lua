require ("public")
local userInfoSP
local IDCardButton
local mobileNum
local isRealName
local firstPasswordView
local secondPasswordView
local picturePath=""
local sum=0
local resetCondition=""
local wayCheck=false

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  IDCardButton=page:getElementById("btn_IDCardID")
  firstPasswordView=page:getElementById("first_pwdID")
  secondPasswordView=page:getElementById("second_pwdID")
  
  mobileNum=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
  if mobileNum == "" then
    setElementHidden(page,"group_notLoginID",false)    
  else
    setElementHidden(page,"group_customerID",false)    
    customerDetail(page)
  end
  onStartImageResult(page)  
end

function onStartImageResult(page)
  local listView=page:getElementById("photograph_listID")
  listView:getEntity():getTemplateData():clear()
  local entityData={}
  for i=1,1,1 do
    entityData[i]={subEntityValues={isPhotographID="photograph",decoment_pictureID="res://icon_picture_add.png"}}
  end
  listView:upDateEntity(entityData)
end


-----^_^.^_^.^_^.^_^.^_^-------------小左边-------------^_^.^_^.^_^.^_^.^_^-----

--登录确定
function onLogin(page)
  mobileNum=getElementValue(page,"mobileID")
  if mobileNum == "" or mobileNum == nil then
    page:showToast("请输入手机号")
  return
  end
  
  customerDetail(page)
end

--获取账户信息
function customerDetail(page)
  local Parameters={
    NetSelector="NetData:",                
    URL="/resetPassword/validateDangerNum.do",
    LuaNetCallBack="CustomerDetailNetCallBack(page,{response})",
    LuaExceptionCallBack="ExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel=""},
    Method="POST",
    ResponseType="JSON",     
    Message="正在加载"
  }
  page:request(Parameters);
end

local dangerNum
function CustomerDetailNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 or response["resultCode"] == 200 or response["resultCode"] == 300 then
    local data=response["response"]["data"]
    setElementValue(page,"customer_mobileID",mobileNum)
    setElementValue(page,"customer_brandID",data["Brand"])
    if data["RealInfoCode"] == "000" or data["RealInfoCode"] == "1" then
      setElementValue(page,"customer_realNameID","已实名登记")
    else
      setElementValue(page,"customer_realNameID","未实名登记")      
    end
    isRealName=data["RealInfoCode"]
    
    if data["Brand"] == "神州行" then
      firstPasswordView:setHint("请输入8位数字")
      secondPasswordView:setHint("请输入8位数字")
    else
      firstPasswordView:setHint("请输入6位数字")
      secondPasswordView:setHint("请输入6位数字")
    end
    
    local customerLoginType=page:getActivity():decrypt(userInfoSP:getString("loginType",""))
    if customerLoginType == "sms" then
      sum=sum+1
      wayCheck=true
      resetCondition=resetCondition.."本机有效SIM卡；"
      setElementHidden(page,"sms_yesID",false)
      setElementHidden(page,"group_smsID",true)          
    end    
    
    if response["resultCode"] == 0 then
      if customerLoginType == "sms" or customerLoginType == "service" or customerLoginType == "" then
        setElementHidden(page,"group_loginID",false)
        onLoginSelected(page,IDCardButton,"IDCard")
      elseif customerLoginType == "IDCard" or customerLoginType == "valid" then
        setElementHidden(page,"btn_submitID",false)    
      end
    elseif response["resultCode"] == 200 or response["resultCode"] == 300 then
      dangerNum=response["resultCode"]
      if response["resultCode"] == 200 then
        setElementValue(page,"danger_wordID","普通号码！请选择2个条件进行验证：")      
      elseif response["resultCode"] == 300 then
        setElementValue(page,"danger_wordID","优质号码！请选择3个条件进行验证：")      
      end
      setElementHidden(page,"group_finishID",true)    
      setElementHidden(page,"group_errorID",false)    
      setElementHidden(page,"group_unFinishID",false)    
    end
    
    setElementHidden(page,"group_customerID",false)    
    setElementHidden(page,"group_notLoginID",true)      
  else
    page:showToast(response["resultMsg"])
  end
end

function ExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--选择本人凭证件or有效证件鉴权
local nextLogin
local loginSelected
function onLoginSelected(page,this,type)
  if nextLogin ~= nil and nextLogin ~= this then
    nextLogin:setSelected(false)
  end
  nextLogin=this
  this:setSelected(true)
  
  if type == "IDCard" then
    setElementHidden(page,"group_validID",true)
    setElementHidden(page,"group_idcardID",false)
  elseif type == "valid" then
    setElementHidden(page,"group_validID",false)
    setElementHidden(page,"group_idcardID",true)
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
local validValueID="IdCard" --证件类型对应的编号
function onLoginSure(page)
  if loginSelected == "IDCard" then
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
  
    validSelectedValue(page,getElementValue(page,"selected_validID"))
    local verType
    if getElementValue(page,"selected_validID") == "身份证" then
      verType="AuthCheckE"
    else
      verType="AuthCheckD"
    end    
    local Parameters={
      NetSelector="NetData:",                
      URL="/login/CardsLogin.do",
      LuaNetCallBack="LoginNetCallBack(page,{response})",
      LuaExceptionCallBack="LoginExceptionCallBack(page)",
      RequestParams={phoneNum=mobileNum,channel="",custType="1",verType=verType,typeValue=validValueID,codeValue=validNum},
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
    setElementHidden(page,"btn_submitID",false)
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
end


-----^_^.^_^.^_^.^_^.^_^-------------办理-------------^_^.^_^.^_^.^_^.^_^-----

local passwordWord
function onTextChanged(page,this,type)
  local first=firstPasswordView:getValue()
  local second=secondPasswordView:getValue()
  if type == "first" then
    passwordWord=first
  elseif type == "second" then
    passwordWord=second
  end
  
  if getElementValue(page,"customer_brandID") == "神州行" then
    if (passwordWord == "00000000" or passwordWord == "11111111" or passwordWord == "22222222" or passwordWord == "33333333" or passwordWord == "44444444" or passwordWord == "55555555" or passwordWord == "66666666" or passwordWord == "77777777" or passwordWord == "88888888" or passwordWord == "99999999" or passwordWord == "01234567" or passwordWord == "12345678" or passwordWord == "23456789" or passwordWord == "34567890" or passwordWord == "09876543" or passwordWord == "98765432" or passwordWord == "87654321" or passwordWord == "76543210" or passwordWord == string.sub(getElementValue(page,"customer_mobileID"),4,11)) and passwordWord ~= "" then
      page:showToast("请不要输入简易密码")
      firstPasswordView:clearText()
      secondPasswordView:clearText()
    end
    
    if string.len(first) == 8 and string.len(second) == 8 and first ~= second then
      page:showToast("两次密码输入不一致")
      firstPasswordView:clearText()
      secondPasswordView:clearText()
    end
  else
    if (passwordWord == "000000" or passwordWord == "111111" or passwordWord == "222222" or passwordWord == "333333" or passwordWord == "444444" or passwordWord == "555555" or passwordWord == "666666" or passwordWord == "777777" or passwordWord == "888888" or passwordWord == "999999" or passwordWord == "012345" or passwordWord == "123456" or passwordWord == "234567" or passwordWord == "345678" or passwordWord == "456789" or passwordWord == "567890" or passwordWord == "098765" or passwordWord == "987654" or passwordWord == "876543" or passwordWord == "765432" or passwordWord == "654321" or passwordWord == "543210" or passwordWord == string.sub(getElementValue(page,"customer_mobileID"),6,11)) and passwordWord ~= "" then
      page:showToast("请不要输入简易密码")
      firstPasswordView:clearText()
      secondPasswordView:clearText()
    end
    
    if string.len(first) == 6 and string.len(second) == 6 and first ~= second then
      page:showToast("两次密码输入不一致")
      firstPasswordView:clearText()
      secondPasswordView:clearText()
    end
  end
end

--业务单详情
function onContractDetail(page)
  local bundle=page:initBundle()
    bundle:putString("protocol","resetPassword")
    bundle:putString("signatureURL",getElementValue(page,"signature_urlID"))
    bundle:putString("mobileNum",getElementValue(page,"customer_mobileID"))
    bundle:putString("resetCondition",resetCondition)
    if isRealName == "000" then
      bundle:putString("businessType","passwordResetReal")
    else
      bundle:putString("businessType","passwordResetNoReal")
    end
    page:setCustomAnimations("left")
    page:nextPage("res://protocolPage.xml",false,bundle) 
end

function onPageResume(page)
  local signaturePath=page:getArguments():getString("signaturePath")
  if signaturePath ~= getElementValue(page,"signature_urlID") and signaturePath ~= nil then
    setElementValue(page,"write_signatureID","local://"..signaturePath)
    setElementValue(page,"signature_urlID",signaturePath)
  end
end

--拍照
function onPhotograph(page,this)
  local isPhotograph=this:getElementById("isPhotographID"):getValue()
  if isPhotograph == "photograph" then
    page:getActivity():photograph(mobileNum,"PasswordReset")
  elseif isPhotograph == "looking" then
    onBigImage(page)
  end
end

function ImageResult(page)
  local path=page:getActivity():getPhotoName()
  picturePath=picturePath..path..","
  local url=split(picturePath,",")
  local pictureSize=table.getn(url)
  local listView=page:getElementById("photograph_listID")
  listView:getEntity():getTemplateData():clear()
  local entityData={}
  for i=1,pictureSize,1 do
    if i < pictureSize then
      entityData[i]={subEntityValues={isPhotographID="looking",decoment_pictureID="local://"..url[i]}}
    elseif i == pictureSize and pictureSize < 6 then
      entityData[i]={subEntityValues={isPhotographID="photograph",decoment_pictureID="res://icon_picture_add.png"}}
    end
  end
  listView:upDateEntity(entityData)
end

--查看大图
function onBigImage(page)
  local popupView=page:getElementById("pop_pictureID")
  local path=split(picturePath,",")
  local pathSize=table.getn(path)
  local listView=page:getElementById("look_imageID")
  listView:getEntity():getTemplateData():clear()
  local entityData={}
  for i=1,pathSize-1,1 do
    entityData[i]={subEntityValues={big_imageID="local://"..path[i]}}
  end
  listView:upDateEntity(entityData)
  popupView:showInFragment("center",0,0)
end

--提交
function onSubmit(page)
  local first=firstPasswordView:getValue()
  local second=secondPasswordView:getValue()
  if first == "" or first == nil then
    page:showToast("请输入密码")
  return
  end
  
  if second == "" or second == nil then
    page:showToast("请再次输入密码")
  return
  end
  
  if first ~= second then
    page:showToast("两次密码输入不一致")
  return
  end
  
  local brand=getElementValue(page,"customer_brandID")
  if brand == "神州行" and string.len(first) ~= 8 then
    page:showToast("请输入8位数字的密码")
  return
  elseif (brand == "动感地带" or brand == "全球通") and string.len(first) ~= 6 then
    page:showToast("请输入6位数字的密码")
  return
  end
  
  local signaturePath=page:getArguments():getString("signaturePath")
  if signaturePath == "" or signaturePath == nil then
    page:showToast("请先签名")
    return
  end
  setElementEnable(page,"btn_submitID",true)
  
  local path=split(picturePath,",")
  local size=table.getn(path)
  local postFilePaths=getImagePostFilePaths(page,signaturePath,path,size)
  local remark=getElementValue(page,"remark_ID")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/imgsUpload.do",
    LuaNetCallBack="SubmitNetCallBack(page,{response})",
    LuaExceptionCallBack="SubmitExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,businessType="重置密码",remark=remark},
    PostFilePaths=postFilePaths,
    Method="FORM",
    ResponseType="JSON",    
    Message="正在提交"
  }
  page:request(Parameters);
end

function SubmitNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    onBusinessHandle(page)
  else
    setElementEnable(page,"btn_submitID",false)
    page:showToast("图片上传失败")
  end
end

function SubmitExceptionCallBack(page)
  setElementEnable(page,"btn_submitID",false)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function onBusinessHandle(page)
  local first=firstPasswordView:getValue()
  local second=secondPasswordView:getValue()
  
  page:getActivity():showProgreesDialog("正在办理")
  local Parameters={
    NetSelector="NetData:",                
    URL="/resetPassword/forcedModyPwd.do",
    LuaNetCallBack="BusinessHandleNetCallBack(page,{response})",
    LuaExceptionCallBack="BusinessHandleExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",IP="",nPassword=first,comfirm=second,remark=""},
    Method="POST",
    ResponseType="JSON",     
--    Message="正在重置"
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
    --倒计时
    onCountDown(page,"btn_resetPasswordID")
  else
    setElementValue(page,"fail_wordID",response["resultMsg"])
    setElementHidden(page,"group_applyID",true)
    setElementHidden(page,"group_failID",false)
  end
end

function BusinessHandleExceptionCallBack(page)
  setElementEnable(page,"btn_submitID",false)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
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


-----^_^.^_^.^_^.^_^.^_^-------------未身份确认-------------^_^.^_^.^_^.^_^.^_^-----

--本机有效SIM卡验证
--获取短信验证码
function onObtain(page)
  local Parameters={
    NetSelector="NetData:",                
    URL="/login/randPwdGenerator.do",
    LuaNetCallBack="onObtainNetCallBack(page,{response})",
    LuaExceptionCallBack="onObtainExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel=""},
    Method="POST",
    ResponseType="JSON",    
    Message="正在获取"
  }
  page:request(Parameters);
end

local nextCountDown=0
function onObtainNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    --倒计时
    nextCountDown=60
    onCountDown(page)
    setElementEnable(page,"count_timeID",true)
    page:showToast("短信验证码已发送成功")
  else
    page:showToast(response["resultMsg"])
  end
end

function onObtainExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--倒计时
function onCountDown(page)
  if nextCountDown ~= 0 then
    nextCountDown=nextCountDown-1
    setElementValue(page,"count_timeID",nextCountDown.."秒")
    page:postDelayed("onCountDown(page)",1000)
  else
    nextCountDown=0
    setElementValue(page,"count_timeID","重新获取")
    setElementEnable(page,"count_timeID",false)
  end
end

function onSMS(page)
  local smsNum=getElementValue(page,"sms_numberID")
  if smsNum == "" or smsNum == nil then
    page:showToast("请输入短信验证码")
  return
  end
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/resetPassword/randomPwdValidate.do",
    LuaNetCallBack="onSMSNetCallBack(page,{response})",
    LuaExceptionCallBack="onSMSExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",codeValue=smsNum},
    Method="POST",
    ResponseType="JSON",    
    Message="正在提交"
  }
  page:request(Parameters);
end

function onSMSNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    sum=sum+1
    wayCheck=true
    resetCondition=resetCondition.."本机有效SIM卡；"
    setElementHidden(page,"sms_yesID",false)
    setElementHidden(page,"sms_noID",true)
    setElementHidden(page,"group_smsID",true)
    onValidateSubmitButton(page)
  else
    setElementHidden(page,"sms_yesID",true)
    setElementHidden(page,"sms_noID",false)
  end    
end

function onSMSExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--充值记录验证
--选择时间
function onSelectedTimeResult(page,year,month,day)
  setElementValue(page,"recharge_timeID",year..month..day)
end

function onRecharge(page)
  local time=getElementValue(page,"recharge_timeID")
  if time == "" or time == nil or time == "请选择充值日期" then
    page:showToast("请选择充值日期")
  return
  end
  
  local money=getElementValue(page,"moneyID")
  if money == "" or money == nil then
    page:showToast("请输入充值金额")
  return
  end
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/resetPassword/validatePaymentRecords.do",
    LuaNetCallBack="RechargeNetCallBack(page,{response})",
    LuaExceptionCallBack="RechargeExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",validateTime=time,amount=money},
    Method="POST",
    ResponseType="JSON",     
    Message="正在提交"
  }
  page:request(Parameters);
end

function RechargeNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    if data["Result"] == "0" then
      setElementHidden(page,"recharge_yesID",true)
      setElementHidden(page,"recharge_noID",false)
    elseif data["Result"] == "1" then  
      sum=sum+1
      resetCondition=resetCondition.."充值记录；"      
      setElementHidden(page,"recharge_yesID",false)
      setElementHidden(page,"recharge_noID",true)
      setElementHidden(page,"group_rechargeID",true)
      onValidateSubmitButton(page)
    end
  else
    setElementHidden(page,"recharge_yesID",true)
    setElementHidden(page,"recharge_noID",false)
  end 
end

function RechargeExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--通话记录验证
function onCallLogs(page)
  local callNum1=getElementValue(page,"call_num1ID")
  local callNum2=getElementValue(page,"call_num2ID")
  local callNum3=getElementValue(page,"call_num3ID")
  if callNum1 == "" or callNum1 == nil or callNum2 == "" or callNum2 == nil or callNum3 == "" or callNum3 == nil then
    page:showToast("请输入手机号码")
  return
  end
  
  if callNum1 == callNum2 or callNum1 == callNum3 or callNum2 == callNum3 then
    page:showToast("请输入3个不同的手机号码")
  return
  end
  
  setElementHidden(page,"call_yesID",true)
  setElementHidden(page,"call_noID",true)
  for i=1,3,1 do
    setElementHidden(page,"num"..i.."_yesID",true)
    setElementHidden(page,"num"..i.."_noID",true)
  end  
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/resetPassword/queryChkNumberVoiceInfo.do",
    LuaNetCallBack="onCallLogsNetCallBack(page,{response})",
    LuaExceptionCallBack="onCallLogsExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",type="1",mobileNum1=callNum1,mobileNum2=callNum2,mobileNum3=callNum3},
    Method="POST",
    ResponseType="JSON",     
    Message="正在提交"
  }
  page:request(Parameters);
end

function onCallLogsNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    if #data == 0 then
      setElementHidden(page,"call_noID",false)
      for i=1,3,1 do
        setElementHidden(page,"num"..i.."_noID",true)
        setElementHidden(page,"num"..i.."_yesID",true)
      end    
    else
      if data[1]["Result"] == 0 or data[2]["Result"] == 0 or data[3]["Result"] == 0 then
        setElementHidden(page,"call_noID",false)
      elseif data[1]["Result"] == 1 and data[2]["Result"] == 1 and data[3]["Result"] == 1 then
        sum=sum+1
        wayCheck=true
        resetCondition=resetCondition.."通话记录；"        
        setElementHidden(page,"call_yesID",false)
        setElementHidden(page,"group_callLogsID",true)
        onValidateSubmitButton(page)
      end
      for i=1,3,1 do
        if data[i]["Result"] == 0 then
          setElementHidden(page,"num"..i.."_noID",false)
        elseif data[i]["Result"] == 1 then
          setElementHidden(page,"num"..i.."_yesID",false)
        end
      end
    end
  else
    page:showToast(response["resultMsg"])
  end
end

function onCallLogsExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--激活时间验证
function onActivationTime(page)
  local year=getElementValue(page,"yearID")
  local month=getElementValue(page,"monthID")
  if year == "" or year == nil then
    page:showToast("请输入年份")
    return
  end
  
  if month == "" or month == nil then
    page:showToast("请输入月份")
    return
  end
  
  if month < "9" and string.len(month) == 1 then
    month="0"..month
  end
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/resetPassword/validateTime.do",
    LuaNetCallBack="onActivationTimeNetCallBack(page,{response})",
    LuaExceptionCallBack="onActivationTimeExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",validateTime=year..month},
    Method="POST",
    ResponseType="JSON",     
    Message="正在提交"
  }
  page:request(Parameters);
end

function onActivationTimeNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    if data["Result"] == "0" then
      setElementHidden(page,"time_yesID",true)
      setElementHidden(page,"time_noID",false)
    elseif data["Result"] == "1" then
      sum=sum+1
      resetCondition=resetCondition.."激活时间；"
      setElementHidden(page,"time_yesID",false)
      setElementHidden(page,"time_noID",true)
      setElementHidden(page,"group_activationID",true)
      onValidateSubmitButton(page)
    end  
  else
    page:showToast(response["resultMsg"])
  end
end

function onActivationTimeExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--业务单据
function onBusiness(page)
  sum=sum+1
  resetCondition=resetCondition.."业务单据；"
  setElementHidden(page,"business_yesID",false)
  setElementHidden(page,"btn_businessID",true)
  onValidateSubmitButton(page)
end

--购卡发票
function onInvoice(page)
  sum=sum+1
  resetCondition=resetCondition.."购卡发票；"
  setElementHidden(page,"invoice_yesID",false)
  setElementHidden(page,"btn_invoiceID",true)
  onValidateSubmitButton(page)
end

--客户注册信息
function onRegisterInfo(page)
  sum=sum+1
  resetCondition=resetCondition.."客户注册信息；"
  setElementHidden(page,"info_yesID",false)
  setElementHidden(page,"btn_infoID",true)
  onValidateSubmitButton(page)
end

--旧包装密码封
function onPacking(page)
  sum=sum+1
  resetCondition=resetCondition.."旧包装密码封；"
  setElementHidden(page,"packing_yesID",false)
  setElementHidden(page,"btn_packingID",true)
  onValidateSubmitButton(page)
end

--新包装SIM卡外卡
function onNewPacking(page)
  sum=sum+1
  resetCondition=resetCondition.."新包装SIM卡外卡；"
  setElementHidden(page,"newPacking_yesID",false)
  setElementHidden(page,"btn_newPackingID",true)
  onValidateSubmitButton(page)
end

--提交验证
function onValidateSubmit(page)
  if dangerNum == 200 then
    if (sum >= 2 and wayCheck == true) or (sum >=3 and wayCheck == false) then
      setElementHidden(page,"btn_submitID",false)    
      setElementHidden(page,"group_finishID",false)
      setElementHidden(page,"group_unFinishID",true)
      setElementHidden(page,"group_errorID",true)
    elseif sum == 2 and wayCheck == false then
      page:showToast("请选择“本机有效SIM卡”或“通话记录”的其中一项验证并通过")
    end  
  elseif dangerNum == 300 then
    if sum >= 3 and wayCheck == true then
      setElementHidden(page,"btn_submitID",false)    
      setElementHidden(page,"group_finishID",false)
      setElementHidden(page,"group_unFinishID",true)
      setElementHidden(page,"group_errorID",true)
    elseif sum >= 3 and wayCheck == false then
      page:showToast("请选择“本机有效SIM卡”或“通话记录”的其中一项验证并通过")
    end
  end  
end

function onValidateSubmitButton(page)
  if dangerNum == 200 then
    if sum >= 2 then
      setElementEnable(page,"btn_validateSubmitID",false)
    elseif sum < 2 then
      setElementEnable(page,"btn_validateSubmitID",true)
    end 
  elseif dangerNum == 300 then
    if sum >= 3 then
      setElementEnable(page,"btn_validateSubmitID",false)
    elseif sum < 3 then
      setElementEnable(page,"btn_validateSubmitID",true)
    end 
  end
end 

function goBack(page)
  page:setCustomAnimations("right")
  page:goBack()  
end

function onBackPressed(page)
  goBack(page)
end