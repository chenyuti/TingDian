require("public")
local userInfoSP
local serviceButton --小左边-客户服务按钮
local mealButton --小左边-4G套餐按钮
local otherButton --小左边-其他业务按钮

local mobileView --客户登录-电话号码
local smsView --客户登录-短信验证码
local serviceView --客户登录-服务密码
local idcardView --客户登录-本人凭证件
local validIDCardView --客户登录-有效证件-身份证
local validOtherView --客户登录-有效证件-其他证件
local smsButton --客户登录-短信验证码按钮
local countTimeView --客户登录-获取短信验证码倒计时

local tips4GView
local updateTipsView
local leaveView --退出弹框

local leftButtonType
local validateType
local isInoffer
local entrance

function PageInit(page)
	isInoffer = "false"
	
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  serviceButton=page:getElementById("btn_serviceID")
  mealButton=page:getElementById("btn_mealID")
  otherButton=page:getElementById("btn_otherID")

  mobileView=page:getElementById("customer_mobileID")
  smsView=page:getElementById("sms_numberID")
  serviceView=page:getElementById("service_numberID")
  idcardView=page:getElementById("idcard_numID")
  validIDCardView=page:getElementById("valid_idcardNumID")
  validOtherView=page:getElementById("valid_otherNumID")
  smsButton=page:getElementById("validate_smsID")
  countTimeView=page:getElementById("count_timeID")

  tips4GView=page:getElementById("4g_tipsID")
  updateTipsView=page:getElementById("update_tipsID")
  leaveView=page:getElementById("leaveID")
  
  phoneNum = page:getElementById("phoneNum")

  local account=page:getActivity():decrypt(userInfoSP:getString("account",""))
  local hall=page:getArguments():getString("hall")
  local today=page:getArguments():getString("today")
  local week=page:getArguments():getString("week")
  setElementValue(page,"user_nameID",account)
  setElementValue(page,"user_hallID",hall)
  setElementValue(page,"today_ID",today.."  "..week)

  local appVersion=page:getActivity():getAppVersion()
  setElementValue(page,"app_versionID","当前版本号：V"..appVersion)

  onLeftHandle(page,serviceButton,"service")
  onValidateSelected(page,smsButton,"sms")

  onUpdate(page)
end

function onViewDidLoad(page)
  local today=page:getArguments():getString("today")
  local nowTime=page:getActivity():getTime(today)
  local updateTime=page:getActivity():decrypt(userInfoSP:getString("updateTime",""))
  local updateTips=userInfoSP:getBoolean("updateTips",false)
  local account=page:getActivity():decrypt(userInfoSP:getString("account",""))
  local accountList=page:getActivity():decrypt(userInfoSP:getString("accountList",""))

  if updateTime ~= "" and tonumber(nowTime) < tonumber(updateTime)+86400*3 and updateTips and string.find(accountList,account..nowTime) == nil then
    onUpdateTips(page)
    local edit=userInfoSP:edit()
    edit:putString("accountList",page:getActivity():encrypt(accountList..account..nowTime..","))
    edit:commit()
  end
end

function onUpdateTips(page)
  local updateTitle=page:getArguments():getString("updateTitle")
  local updateContent=page:getArguments():getString("updateContent")
  local updateVersion=page:getArguments():getString("updateVersion")
  setElementValue(page,"update_tipsTitleID",updateTitle)
  setElementValue(page,"update_tipsWordID",string.gsub(updateContent, "newline", "\n"))
  updateTipsView:showInFragment("center",0,0)
end

function onUpdateTipsDismiss(page)
  updateTipsView:dismiss()
end

function onPageResume(page)
  local mobile=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
  local meal4G=page:getArguments():getString("4GMeal")
  local broadbandEntrance=page:getArguments():getString("broadbandEntrance")
  local customService=page:getArguments():getString("customService")

if customService == "customService" and leftButtonType == "offers" then
    onLeftHandle(page,serviceButton,"service")
  end
  if meal4G == "4GMeal" and leftButtonType == "offers" then
    onLeftHandle(page,mealButton,"meal")
  end
  if meal4G == "4GMeal" and leftButtonType == "service" then
    onLeftHandle(page,mealButton,"meal")
  end

  if mobile ~= "" and leftButtonType == "service" and meal4G ~= "4GMeal" then
    onLeftHandle(page,otherButton,"other")
  end

  if broadbandEntrance == "broadband" and leftButtonType == "other" and entrance == "broadband" then
    entrance="other"
    onLeftHandle(page,serviceButton,"service")
  end
end


-----^_^.^_^.^_^.^_^.^_^-------------小左边-------------^_^.^_^.^_^.^_^.^_^-----

function onMySetting(page)
  page:setCustomAnimations("left")
  page:nextPage("res://devicePage.xml",false,nil)
end

--注销
function onLoginOut(page)
	
  local Parameters={
    NetSelector="NetData:",
    URL="/login/employeeValidate.do",
    LuaNetCallBack="LoginOutNetCallBack(page,{response})",
    LuaExceptionCallBack="LoginOutExceptionCallBack(page)",
    RequestParams={},
    Method="POST",
    ResponseType="JSON",
    Message="正在注销"
  }
  page:request(Parameters);
end

function LoginOutNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    page:setCustomAnimations("right")
    page:nextPage("res://loginPage.xml",true,nil)
  else
    page:showToast(response["resultMsg"])
  end
end

function LoginOutExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

local lastHandle
function onLeftHandle(page,this,value)
  if lastHandle ~= nil and lastHandle ~= this then
    lastHandle:setSelected(false)
  end
  lastHandle=this
  this:setSelected(true)

  leftButtonType=value
  setElementHidden(page,"group_serviceID",true)
  setElementHidden(page,"group_mealID",true)
  setElementHidden(page,"group_coverID",true)
  setElementHidden(page,"group_otherID",true)
  setElementHidden(page,"group_feedbackID",true)
  setElementHidden(page,"group_newsID",true)
  setElementHidden(page,"group_offerID",true)
  setElementHidden(page,"group_loginID",true)
  if value == "service" then
  isInoffer = "false"
    setElementHidden(page,"group_serviceID",false)
    local customerMobile=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
    if customerMobile ~= "" then
      local bundle=page:initBundle()
      bundle:putString("authenticationStyle",validateType)
      bundle:putString("mobileNum",customerMobile)
      page:setCustomAnimations("left")
      page:nextPage("res://customerServicePage.xml",false,bundle)
    end
  elseif value == "meal" then
  isInoffer = "false"
    local customerMobile=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
    local customerBrand=page:getActivity():decrypt(userInfoSP:getString("brand",""))
    local customerMealName=page:getActivity():decrypt(userInfoSP:getString("menu",""))
    setElementValue(page,"meal_mobileID",customerMobile)
    setElementValue(page,"use_mealNameID",customerBrand.." | "..customerMealName)

    setElementHidden(page,"group_mealID",false)
    local flyButton=page:getElementById("fly_mealID")
    on4GMealType(page,flyButton,"fly")

    local customerMobile=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
    if customerMobile == "" then
      onLeftHandle(page,serviceButton,"service")
      page:showToast("请先登录")
      return
    end

    tips4GView:showInFragment("center",0,0)
  elseif value == "cover" then
  isInoffer = "false"
    setElementHidden(page,"group_coverID",false)

  elseif value == "other" then
  isInoffer = "false"
    setElementHidden(page,"group_otherID",false)
    --  新增业务优惠查询
  elseif value == "offers" then
     isInoffer = "true"
     setElementValue(page,"phoneNum","")
     setElementHidden(page,"group_loginID",false)

    --  调用java代码
    

  elseif value == "feedback" then
  isInoffer = "false"
    setElementHidden(page,"group_feedbackID",false)
    

  elseif value == "news" then
  isInoffer = "false"
    setElementHidden(page,"group_newsID",false)
    onNewsList(page)
  end
end


function next(page)
		local customerMobile=page:getActivity():decrypt(userInfoSP:getString("phoneNum",""))
		--page:showToast(customerMobile)
		local bundle=page:initBundle()
      	--bundle:putString("authenticationStyle",validateType)
      	bundle:putString("mobileNum",customerMobile)
      	page:setCustomAnimations("left")
      	page:nextPage("res://authServicePage.xml",false,bundle)
end 

--本次服务已完成
function onCustomerQuit(page)
  local mobileNum=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
  if mobileNum == "" then
    page:showToast("暂无客户登录")
    return
  end
if isInoffer == "true" then
    setElementHidden(page,"group_loginID",false)
  	setElementHidden(page,"group_offerID",true)
  	setElementValue(page,"phoneNum","")
  	end
  local Parameters={
    NetSelector="NetData:",
    URL="/login/userValidate.do",
    LuaNetCallBack="CustomerQuitNetCallBack(page,{response})",
    LuaExceptionCallBack="CustomerQuitExceptionCallBack(page)",
    RequestParams={},
    Method="POST",
    ResponseType="JSON",
    Message="正在注销"
  }
  page:request(Parameters);
end

function CustomerQuitNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local edit=userInfoSP:edit()
    edit:putString("mobile","")
    edit:putString("loginType","")
    edit:putString("brand","")
    edit:putString("realName","")
    edit:putString("menu","")
    edit:putString("stopOn","")
    edit:putString("cardType","")
    edit:putString("balance","")
    edit:putString("usimCard","")
    edit:putString("userName","")
    edit:putString("userNum","")
    edit:putString("userAddress","")
    edit:putString("voiceLength","")
    edit:putString("voiceSurplus","")
    edit:putString("flowLength","")
    edit:putString("flowSurplus","")
    edit:putString("broadband","")
    edit:putString("broadbandEnd","")
    edit:putString("managers","")
    edit:putString("phoneNum","")
    edit:commit()

    page:showToast("注销成功")
    
    page:getActivity():deleteSCSSAlbum()
  else
    page:showToast(response["resultMsg"])
  end
end

function CustomerQuitExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--检测新版本
function onUpdate(page)
  local Parameters={
    NetSelector="NetData:",
    URL="/appUpdate/update.do",
    LuaNetCallBack="UpdateNetCallBack(page,{response})",
    LuaExceptionCallBack="UpdateExceptionCallBack(page)",
    RequestParams={},
    Method="POST",
    ResponseType="JSON",
  --    Message="正在检测"
  }
  page:request(Parameters);
end

function UpdateNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 100 then
    local prefixURL=page:getActivity():getPrefixURL()
    local updateURL=prefixURL..response["response"]["data"]["Path"]
    local VersionUtil=luajava.bindClass("com.logansoft.UIEngine.utils.VersionUtil"):instance(page:getActivity(),updateURL)
    VersionUtil:showUpdataDialog("有新版本更新")

    local today=page:getArguments():getString("today")
    local updateTime=page:getActivity():getTime(today)
    local edit=userInfoSP:edit()
    edit:putString("updateTime",page:getActivity():encrypt(updateTime))
    edit:putBoolean("updateTips",true)
    edit:putString("accountList","")
    edit:putString("isActivated","")
    edit:commit()
  end
end

function UpdateExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end


-----^_^.^_^.^_^.^_^.^_^-------------客户服务-------------^_^.^_^.^_^.^_^.^_^-----

local loginType
local lastValidate
function onValidateSelected(page,this,value)
  if lastValidate ~= nil and lastValidate ~= this then
    lastValidate:setSelected(false)
  end
  lastValidate=this
  this:setSelected(true)

  smsView:clearText()
  serviceView:clearText()
  idcardView:clearText()
  validIDCardView:setValue("")
  validOtherView:clearText()

  setElementHidden(page,"view_smsID",true)
  setElementHidden(page,"view_serviceID",true)
  setElementHidden(page,"view_idcardID",true)
  setElementHidden(page,"view_documentID",true)
  loginType=value
  if value == "sms" then
    setElementHidden(page,"view_smsID",false)
  elseif value == "service" then
    setElementHidden(page,"view_serviceID",false)
  elseif value == "identification" then
    setElementHidden(page,"view_idcardID",false)
  elseif value == "document" then
    setElementHidden(page,"view_documentID",false)
  end
end

--查询业务
function onCustomerService(page)
  if loginType == "sms" then
    onSMSValidate(page)
  elseif loginType == "service" then
    onServicePasswordValidate(page)
  elseif loginType == "identification" then
    onIdentificationValidate(page)
  elseif loginType == "document" then
    onDocumentValidate(page)
  end
end

--获取短信验证码
function onObtain(page)
  local mobileNum=getElementValue(page,"customer_mobileID")
  if string.len(mobileNum) ~= 11 then
    page:showToast("请输入正确的手机号")
    return
  end

  local Parameters={
    NetSelector="NetData:",
    URL="/login/randPwdGenerator.do",
    LuaNetCallBack="ObtainNetCallBack(page,{response})",
    LuaExceptionCallBack="ObtainExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel=""},
    Method="POST",
    ResponseType="JSON",
    Message="正在获取"
  }
  page:request(Parameters);
end

local nextCountDown=0
function ObtainNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    --倒计时
    nextCountDown=60
    onCountDown(page)
    countTimeView:setDisable(true)
    page:showToast("短信验证码已发送成功")
  else
    page:showToast(response["resultMsg"])
  end
end

function ObtainExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--倒计时
function onCountDown(page)
  if nextCountDown ~= 0 then
    nextCountDown=nextCountDown-1
    countTimeView:setValue(nextCountDown.."秒")
    page:postDelayed("onCountDown(page)",1000)
  else
    nextCountDown=0
    countTimeView:setValue("重新获取")
    countTimeView:setDisable(false)
  end
end

--短信验证登录鉴权
function onSMSValidate(page)
  local mobileNum=getElementValue(page,"customer_mobileID")
  local smsNum=getElementValue(page,"sms_numberID")
  if string.len(mobileNum) ~= 11 then
    page:showToast("请输入正确的手机号")
    return
  end

  if smsNum == "" or smsNum == nil then
    page:showToast("请输入短信验证码")
    return
  end

  local Parameters={
    NetSelector="NetData:",
    URL="/login/CardsLogin.do",
    LuaNetCallBack="SMSValidateNetCallBack(page,{response})",
    LuaExceptionCallBack="SMSValidateExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",custType="1",verType="AuthCheckA",typeValue="randompwd",codeValue=smsNum},
    Method="POST",
    ResponseType="JSON",
    Message="正在查询"
  }
  page:request(Parameters);
end

function SMSValidateNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    nextCountDown=0
    saveCustomerDetail(page,mobileView:getValue(),"sms")
    page:setCustomAnimations("left")
    page:nextPage("res://customerServicePage.xml",false,nil)
    clearCustomer()
  else
    page:showToast(response["resultMsg"])
  end
end

function SMSValidateExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--服务密码登录鉴权
function onServicePasswordValidate(page)
  local mobileNum=getElementValue(page,"customer_mobileID")
  local serviceNum=getElementValue(page,"service_numberID")
  if string.len(mobileNum) ~= 11 then
    page:showToast("请输入正确的手机号")
    return
  end

  if serviceNum == "" or serviceNum == nil then
    page:showToast("请输入服务密码")
    return
  end
  local p = page:getActivity():encryptCs(serviceNum)
  local Parameters={
    NetSelector="NetData:",
    URL="/login/pwdLogin2.do",
    LuaNetCallBack="ServicePasswordValidateNetCallBack(page,{response})",
    LuaExceptionCallBack="ServicePasswordValidateExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,pwd=p,channel=""},
    Method="POST",
    ResponseType="JSON",
    Message="正在查询"
  }
  page:request(Parameters);
end

function ServicePasswordValidateNetCallBack(page,response)
  page:log("********zjw************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    saveCustomerDetail(page,mobileView:getValue(),"service")
    page:setCustomAnimations("left")
    page:nextPage("res://customerServicePage.xml",false,nil)
    clearCustomer()
  else
    page:showToast(response["resultMsg"])
  end
end

function ServicePasswordValidateExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function onValueChange(page,this)
  local selectedDocument=getElementValue(page,"selected_documentID")
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
    idcardView:clearText()
    setElementValue(page,"idcard_numID",card[3])
  elseif scanningCard == "valid" then
    validIDCardView:setValue("")
    setElementValue(page,"valid_idcardNumID",card[3])
  end
end

function onIdentificationValidate(page)
  local mobileNum=getElementValue(page,"customer_mobileID")
  local idcardNum=getElementValue(page,"idcard_numID")
  if string.len(mobileNum) ~= 11 then
    page:showToast("请输入正确的手机号")
    return
  end

  if idcardNum == "" or idcardNum == nil then
    page:showToast("请输入身份证号码")
    return
  end
  
  local p = page:getActivity():encryptCs(idcardNum)

  local Parameters={
    NetSelector="NetData:",
    URL="/login/CardsLogin2.do",
    LuaNetCallBack="IdentificationValidateNetCallBack(page,{response})",
    LuaExceptionCallBack="IdentificationValidateExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",custType="1",verType="AuthCheckE",typeValue="IdCard",codeValue=p},
    Method="POST",
    ResponseType="JSON",
    Message="正在查询"
  }
  page:request(Parameters);
end

function IdentificationValidateNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    validateType="identification"
    saveCustomerDetail(page,mobileView:getValue(),"IDCard")
    page:setCustomAnimations("left")
    page:nextPage("res://customerServicePage.xml",false,nil)
    clearCustomer()
  else
    page:showToast(response["resultMsg"])
  end
end

function IdentificationValidateExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--有效证件登录鉴权
local documentTypeValue="IdCard" --证件类型对应的编号
local validNum
function onDocumentValidate(page)
  local mobileNum=getElementValue(page,"customer_mobileID")
  if string.len(mobileNum) ~= 11 then
    page:showToast("请输入正确的手机号")
    return
  end

  local selectedDocument=getElementValue(page,"selected_documentID")
  if selectedDocument == "身份证" then
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

  local verType
  if selectedDocument == "身份证" then
    verType="AuthCheckE"
  else
    verType="AuthCheckD"
  end

  local typeValue=selectedDocumentType(page,selectedDocument)
  local Parameters={
    NetSelector="NetData:",
    URL="/login/CardsLogin.do",
    LuaNetCallBack="DocumentValidateNetCallBack(page,{response})",
    LuaExceptionCallBack="DocumentValidateExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",custType="1",verType=verType,typeValue=typeValue,codeValue=validNum},
    Method="POST",
    ResponseType="JSON",
    Message="正在查询"
  }
  page:request(Parameters);
end

function DocumentValidateNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    saveCustomerDetail(page,mobileView:getValue(),"valid")
    page:setCustomAnimations("left")
    page:nextPage("res://customerServicePage.xml",false,nil)
    clearCustomer()
  else
    page:showToast(response["resultMsg"])
  end
end

function DocumentValidateExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function selectedDocumentType(page,value)
  if value == "身份证" then
    documentTypeValue="IdCard"
  elseif value == "士兵证" then
    documentTypeValue="SoldierID"
  elseif value == "警官证" then
    documentTypeValue="PolicePaper"
  elseif value == "护照" then
    documentTypeValue="Passport"
  elseif value == "户口簿" then
    documentTypeValue="HuKouBu"
  elseif value == "港澳证" then
    documentTypeValue="HKIdCard"
  elseif value == "VIP卡" then
    documentTypeValue="IdVipCard"
  elseif value == "公章" then
    documentTypeValue="GongZhang"
  elseif value == "临时身份证" then
    documentTypeValue="TempId"
  elseif value == "驾驶证" then
    documentTypeValue="DriverIC"
  elseif value == "军官证" then
    documentTypeValue="PLA"
  elseif value == "单位介绍信" then
    documentTypeValue="IdTypeJSX"
  elseif value == "学生证" then
    documentTypeValue="StudentID"
  elseif value == "教师证" then
    documentTypeValue="TeacherID"
  elseif value == "复员证" then
    documentTypeValue="FuYuanZheng"
  elseif value == "暂住证" then
    documentTypeValue="ZanZhuZheng"
  elseif value == "还乡证" then
    documentTypeValue="RetNative"
  elseif value == "其他" then
    documentTypeValue="OtherLicence"
  elseif value == "营业执照" then
    documentTypeValue="BusinessLicence"
  elseif value == "事业单位法人证书" then
    documentTypeValue="CorpLicence"
  elseif value == "单位证明" then
    documentTypeValue="UnitID"
  elseif value == "社会团体法人登记证书" then
    documentTypeValue="OrgaLicence"
  end
  return documentTypeValue
end


-----^_^.^_^.^_^.^_^.^_^-------------4G套餐-------------^_^.^_^.^_^.^_^.^_^-----

--温馨提示
function on4GTips(page)
  tips4GView:dismiss()
end

--飞享套餐、手机流量套餐、流量叠加包选择
local lastMeal
function on4GMealType(page,this,type)
  if lastMeal ~= nil and lastMeal ~= this then
    lastMeal:setSelected(false)
  end
  lastMeal=this
  this:setSelected(true)

  setElementHidden(page,"group_flyID",true)
  setElementHidden(page,"group_flowID",true)
  setElementHidden(page,"group_overlyingID",true)
  --  setElementHidden(page,"group_kingID",true)
  if type=="fly" then
    setElementHidden(page,"group_flyID",false)
  elseif type=="flow" then
    setElementHidden(page,"group_flowID",false)
  elseif type=="overlying" then
    setElementHidden(page,"group_overlyingID",false)
    --  elseif type=="king" then
    --    setElementHidden(page,"group_kingID",false)
  end
end

--飞享套餐选择
local flySelected
local flyID
local lastFly
function onFlyMeal(page,this,id)
  flySelected=this:getValue()
  flyID=id
  if lastFly ~= nil and lastFly ~= this then
    lastFly:setSelected(false)
  end
  lastFly=this
  this:setSelected(true)
end

--手机流量套餐选择
local flowSelected
local flowID
local lastFlow
function onFlowMeal(page,this,id)
  flowSelected=this:getValue()
  flowID=id
  if lastFlow ~= nil and lastFlow ~= this then
    lastFlow:setSelected(false)
  end
  lastFlow=this
  this:setSelected(true)
end

--流量叠加包选择
local overlyingSelected
local overlyingID
local lastOverlying
function onOverlyingMeal(page,this,id)
  overlyingSelected=this:getValue()
  overlyingID=id
  if lastOverlying ~= nil and lastOverlying ~= this then
    lastOverlying:setSelected(false)
  end
  lastOverlying=this
  this:setSelected(true)
end

--流量王套餐选择
local kingSelected
local kingID
local nextKing
function onKingMeal(page,this,id)
  kingSelected=this:getValue()
  kingID=id
  if nextKing ~= nil and nextKing ~= this then
    nextKing:setSelected(false)
  end
  nextKing=this
  this:setSelected(true)
end

local mealBusinessID --办理套餐对应的ID
local selectedMeal --办理套餐的名字
local selectedMealType --办理套餐类型，fly飞享套餐，flow流量套餐，overlying流量叠加包
--下一步
function onMealNextStep(page,type)
  selectedMealType=type
  local realName=page:getActivity():decrypt(userInfoSP:getString("realName",""))
  if realName ~= "000" and realName ~= "1" then
    page:showToast("未实名登记，无法办理该业务！")
    return
  end

  if type == "fly" then
    if flySelected == "" or flySelected == nil then
      page:showToast("请选择飞享套餐")
      return
    end
    local bundle=page:initBundle()
    bundle:putString("mealBusinessID",flyID)
    bundle:putString("mealType",type)
    bundle:putString("mealName",flySelected)
    page:setCustomAnimations("left")
    page:nextPage("res://4GMealPage.xml",false,bundle)
  elseif type == "flow" then
    if flowSelected == "" or flowSelected == nil then
      page:showToast("请选择手机流量套餐")
      return
    end
    mealBusinessID=flowID
    selectedMeal=flowSelected
    onFlowOverlying(page)
  elseif type == "overlying" then
    if overlyingSelected == "" or overlyingSelected == nil then
      page:showToast("请选择流量叠加包")
      return
    end
    mealBusinessID=overlyingID
    selectedMeal=overlyingSelected
    onFlowOverlying(page)
  elseif type == "king" then
    if kingSelected == "" or kingSelected == nil then
      page:showToast("请选择流量王套餐")
      return
    end
    local bundle=page:initBundle()
    bundle:putString("mealBusinessID",kingID)
    bundle:putString("mealType",type)
    bundle:putString("mealName",kingSelected)
    page:setCustomAnimations("left")
    page:nextPage("res://4GMealPage.xml",false,bundle)
  end
end

function onFlowOverlying(page)
  local mobileNum=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
  local Parameters={
    NetSelector="NetData:",
    URL="/client/handleOrderCheck.do",
    LuaNetCallBack="FlowOverlyingNetCallBack(page,{response})",
    LuaExceptionCallBack="FlowOverlyingExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",custID="",custType="1",opType="0",prodId=mealBusinessID,effType="0",bookingFlag="0",bookingTime=""},
    Method="POST",
    ResponseType="JSON",
    Message="正在加载"
  }
  page:request(Parameters);
end

function FlowOverlyingNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    local bundle=page:initBundle()
    bundle:putString("mealBusinessID",mealBusinessID)
    bundle:putString("mealType",selectedMealType)
    bundle:putString("mealName",selectedMeal)
    bundle:putString("orderNum",data["OrderSeq"])
    bundle:putString("discount",data["DeratePrice"])
    bundle:putString("actual",data["Fee"])
    page:setCustomAnimations("left")
    page:nextPage("res://4GMealPage.xml",false,bundle)
  else
    page:showToast(response["resultMsg"])
  end
end

function FlowOverlyingExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end


-----^_^.^_^.^_^.^_^.^_^-------------家宽覆盖查询-------------^_^.^_^.^_^.^_^.^_^-----

local areaWord="福田区"
function onCoverValueChange(page,this)
  local areaValue=getElementValue(page,"areaID")
  local searchValue=getElementValue(page,"searchID")
  if areaValue ~= areaWord and searchValue ~= "" then
    areaWord=areaValue
    onAddressList(page,areaValue,searchValue)
  end
end

local searchWord=""
function onCoverTextChanged(page,this)
  local areaValue=getElementValue(page,"areaID")
  local searchValue=getElementValue(page,"searchID")
  if searchValue ~= searchWord then
    areaWord=areaValue
    searchWord=searchValue
    if searchValue == "" then
      setElementHidden(page,"address_listID",true)
    elseif searchValue ~= "" then
      setElementEnable(page,"btn_openID",true)
      page:getActivity():sleep(page,nil,"onSearchList(page,"..searchValue..")",500)
    end
  elseif searchValue == searchWord and searchWord ~= "" then
    setElementEnable(page,"btn_openID",false)
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

local selectedAddress
function onAddressSelected(page,this)
  selectedAddress=this:getElementById("address_nameID"):getValue()
  searchWord=selectedAddress
  setElementValue(page,"searchID",selectedAddress)
  setElementHidden(page,"address_listID",true)
end

--马上开通宽带
function onNowOpen(page)
  local mobile=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
  if mobile == "" then
    onLeftHandle(page,serviceButton,"service")
    page:showToast("请先登录")
  else
    local bundle=page:initBundle()
    bundle:putString("dealEntrance","cover")
    bundle:putString("newAddress",selectedAddress)
    page:setCustomAnimations("left")
    page:nextPage("res://broadbandPage.xml",false,bundle)
  end
end


-----^_^.^_^.^_^.^_^.^_^-------------其他业务-------------^_^.^_^.^_^.^_^.^_^-----

function onNextService(page,this,url)
  local customerMobile=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
  if customerMobile == "" then
    onLeftHandle(page,serviceButton,"service")
    page:showToast("请先登录")
    return
  end

  page:setCustomAnimations("left")
  page:nextPage(url,false,nil)
end

function onNextPage(page,this,url)
  if url == "res://broadbandPage.xml" then
    entrance="broadband"
  end
  page:setCustomAnimations("left")
  page:nextPage(url,false,nil)
end


-----^_^.^_^.^_^.^_^.^_^-------------系统反馈-------------^_^.^_^.^_^.^_^.^_^-----

local scoreSum
local realNameScore="0"
local stopOnScore="0"
local paymentScore="0"
local replacementCardScore="0"
local resetPasswordScore="0"
local prestoreScore="0"
local openAccountScore="0"
local fallbackScore="0"
local broadbandScore="0"
local menuScore="0"
function onScore(page,this,type,score)
  if score == "1" then
    if this:isSelected() then
      setElementSelected(page,"one_"..type.."ID",false)
      setElementSelected(page,"two_"..type.."ID",false)
      setElementSelected(page,"three_"..type.."ID",false)
      setElementSelected(page,"four_"..type.."ID",false)
      setElementSelected(page,"five_"..type.."ID",false)
      scoreSum="0"
    else
      setElementSelected(page,"one_"..type.."ID",true)
      setElementSelected(page,"two_"..type.."ID",false)
      setElementSelected(page,"three_"..type.."ID",false)
      setElementSelected(page,"four_"..type.."ID",false)
      setElementSelected(page,"five_"..type.."ID",false)
      scoreSum="1"
    end
  elseif score == "2" then
    if this:isSelected() then
      setElementSelected(page,"two_"..type.."ID",false)
      setElementSelected(page,"three_"..type.."ID",false)
      setElementSelected(page,"four_"..type.."ID",false)
      setElementSelected(page,"five_"..type.."ID",false)
      scoreSum="1"
    else
      setElementSelected(page,"one_"..type.."ID",true)
      setElementSelected(page,"two_"..type.."ID",true)
      setElementSelected(page,"three_"..type.."ID",false)
      setElementSelected(page,"four_"..type.."ID",false)
      setElementSelected(page,"five_"..type.."ID",false)
      scoreSum="2"
    end
  elseif score == "3" then
    if this:isSelected() then
      setElementSelected(page,"three_"..type.."ID",false)
      setElementSelected(page,"four_"..type.."ID",false)
      setElementSelected(page,"five_"..type.."ID",false)
      scoreSum="2"
    else
      setElementSelected(page,"one_"..type.."ID",true)
      setElementSelected(page,"two_"..type.."ID",true)
      setElementSelected(page,"three_"..type.."ID",true)
      setElementSelected(page,"four_"..type.."ID",false)
      setElementSelected(page,"five_"..type.."ID",false)
      scoreSum="3"
    end
  elseif score == "4" then
    if this:isSelected() then
      setElementSelected(page,"four_"..type.."ID",false)
      setElementSelected(page,"five_"..type.."ID",false)
      scoreSum="3"
    else
      setElementSelected(page,"one_"..type.."ID",true)
      setElementSelected(page,"two_"..type.."ID",true)
      setElementSelected(page,"three_"..type.."ID",true)
      setElementSelected(page,"four_"..type.."ID",true)
      setElementSelected(page,"five_"..type.."ID",false)
      scoreSum="4"
    end
  elseif score == "5" then
    if this:isSelected() then
      setElementSelected(page,"five_"..type.."ID",false)
      scoreSum="4"
    else
      setElementSelected(page,"one_"..type.."ID",true)
      setElementSelected(page,"two_"..type.."ID",true)
      setElementSelected(page,"three_"..type.."ID",true)
      setElementSelected(page,"four_"..type.."ID",true)
      setElementSelected(page,"five_"..type.."ID",true)
      scoreSum="5"
    end
  end

  if type == "realName" then
    realNameScore=scoreSum
  elseif type == "stopOn" then
    stopOnScore=scoreSum
  elseif type == "payment" then
    paymentScore=scoreSum
  elseif type == "replacementCard" then
    replacementCardScore=scoreSum
  elseif type == "resetPassword" then
    resetPasswordScore=scoreSum
  elseif type == "prestore" then
    prestoreScore=scoreSum
  elseif type == "openAccount" then
    openAccountScore=scoreSum
  elseif type == "fallback" then
    fallbackScore=scoreSum
  elseif type == "broadband" then
    broadbandScore=scoreSum
  elseif type == "menu" then
    menuScore=scoreSum
  end
end

--反馈提交
function onFeedbackSubmit(page)
  local realNameWord=getElementValue(page,"word_realNameID")
  local stopOnWord=getElementValue(page,"word_stopOnID")
  local paymentWord=getElementValue(page,"word_paymentID")
  local replacementCardWord=getElementValue(page,"word_replacementCardID")
  local resetPasswordWord=getElementValue(page,"word_resetPasswordID")
  local prestoreWord=getElementValue(page,"word_prestoreID")
  local openAccountWord=getElementValue(page,"word_openAccountID")
  local fallbackWord=getElementValue(page,"word_fallbackID")
  local broadbandWord=getElementValue(page,"word_broadbandID")
  local menuWord=getElementValue(page,"word_menuID")

  local Parameters={
    NetSelector="NetData:",
    URL="/client/feedback.do",
    LuaNetCallBack="FeedbackSubmitNetCallBack(page,{response})",
    LuaExceptionCallBack="FeedbackSubmitExceptionCallBack(page)",
    RequestParams={fourG=menuScore,fourRemark=menuWord,realNameResign=realNameScore,realNameResignRemark=realNameWord,startAndStopMachine=stopOnScore,startAndStopMachineRemark=stopOnWord,payCharge=paymentScore,payChargeRemark=paymentWord,backupCard=replacementCardScore,backupCardRemark=replacementCardWord,resetPassword=resetPasswordScore,resetPasswordRemark=resetPasswordWord,storeFee=prestoreScore,storeFeeRemark=prestoreWord,openAccount=openAccountScore,openAccountRemark=openAccountWord,broadband=broadbandScore,broadbandRemark=broadbandWord,oneKeyBack=fallbackScore,oneKeyBackRemark=fallbackWord},
    Method="POST",
    ResponseType="JSON",
    Message="正在加载"
  }
  page:request(Parameters)
end

function FeedbackSubmitNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    page:showToast("反馈提交成功")
  else
    page:showToast(response["resultMsg"])
  end
end

function FeedbackSubmitExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end


-----^_^.^_^.^_^.^_^.^_^-------------消息盒子-------------^_^.^_^.^_^.^_^.^_^-----

function onNewsList(page)
  local Parameters={
    NetSelector="NetData:",
    URL="/appUpdate/openMessageBox.do",
    LuaNetCallBack="onNewsListNetCallBack(page,{response})",
    LuaExceptionCallBack="onNewsListExceptionCallBack(page)",
    RequestParams={},
    Method="POST",
    ResponseType="JSON",
    Message="正在加载"
  }
  page:request(Parameters)
end

function onNewsListNetCallBack(page,response)
  page:log("*************************消息盒子"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]

    local listView=page:getElementById("news_listID")
    listView:getEntity():clearTemplateData()
    local entityData={}
    for i=1,#data,1 do
      entityData[i]={subEntityValues={news_titleID=data[i]["Title"],news_timeID=data[i]["LastTime"],news_contentID=string.gsub(data[i]["Detail"], "newline", "\n"),news_versionID="版本号：V"..data[i]["AppVersion"]}}
    end
    listView:upDateEntity(entityData)
  else
    page:showToast(response["resultMsg"])
  end
end

function onNewsListExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--是否退出
function onBackPressed(page)
  leaveView:showInFragment("center",0,0)
end

function outSure(page)
  leaveView:dismiss()
  page:getActivity():exit()
end

function outCancle(page)
  leaveView:dismiss()
end

function saveCustomerDetail(page,mobile,loginType)
  local edit=userInfoSP:edit()
  edit:putString("mobile",page:getActivity():encrypt(mobile))
  edit:putString("loginType",page:getActivity():encrypt(loginType))
  edit:putString("managers",page:getActivity():encrypt("false"))
  edit:commit()
end

function clearCustomer(page)
  mobileView:clearText()
  smsView:clearText()
  serviceView:clearText()
  idcardView:clearText()
  validIDCardView:setValue("")
  validOtherView:clearText()
end


--免鉴权业务
local num
function toAuth(page)
  local mobile=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
 -- local phoneNum = page:getElementById("phoneNum")
  local phoneNum=getElementValue(page,"phoneNum")
  if string.len(phoneNum) ~= 11 then 
  page:showToast("请输入正确的手机号")
  else
  --page:showToast(phoneNum)
  num = phoneNum
  	setElementHidden(page,"group_loginID",true)
  	setElementHidden(page,"group_offerID",false)
  	local edit=userInfoSP:edit()
  	edit:putString("phoneNum",page:getActivity():encrypt(phoneNum))
    edit:commit()
  
  end 
end


