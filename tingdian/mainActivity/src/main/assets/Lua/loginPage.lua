require("public")
local userInfoSP
local isRemember=false --是否记住密码，true是，false否
local smsButton
local nextCountDown=0

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  smsButton=page:getElementById("btn_smsID")
  
  local account=page:getActivity():decrypt(userInfoSP:getString("account",""))
  local password=page:getActivity():decrypt(userInfoSP:getString("password",""))
  local isActivated=page:getActivity():decrypt(userInfoSP:getString("isActivated",""))
  
  if account ~= "" then
    setElementValue(page,"accountID",account)
  end
  
  if password ~= "" then
    setElementValue(page,"passwordID",password)
    setElementSelected(page,"btn_isRememberID",true)
    isRemember=true
  end
  
--  --是否激活，软件第一次使用和更新后需要激活码激活，isActivated="yes"代表已激活
--  if isActivated == "yes" then
--    setElementHidden(page,"is_activatedID",true)
--  else
--    setElementValue(page,"tipsID","请前往“厅店后台——>激活码”菜单\n申请激活码：")
--    setElementHidden(page,"is_activatedID",false)
--  end
end

--激活
function onIsActivatedSure(page)
  local activationCode=getElementValue(page,"activation_codeID")
  if string.len(activationCode) ~= 10 then
    page:showToast("请输入10位数的激活码")
  return
  end
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/appUpdate/validateActivateCode.do",
    LuaNetCallBack="onIsActivatedSureNetCallBack(page,{response})",
    LuaExceptionCallBack="onIsActivatedSureExceptionCallBack(page)",
    RequestParams={activateCode=activationCode},
    Method="POST",
    ResponseType="JSON",    
    Message="正在激活"
  }
  page:request(Parameters);
end

function onIsActivatedSureNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    setElementHidden(page,"is_activatedID",true)
    local edit=userInfoSP:edit()
    edit:putString("isActivated",page:getActivity():encrypt("yes"))
    edit:commit()    
  else
    page:showToast(response["resultMsg"])
  end
end

function onIsActivatedSureExceptionCallBack(page)
  page:hideProgreesDialog()
end  
  
--是否记住密码
function onRemember(page,this)
  local remember=this:isSelected()
  if remember then
    isRemember=false
    this:setSelected(false)
  else
    isRemember=true
    this:setSelected(true)
  end
end


-----^_^.^_^.^_^.^_^.^_^-------------自用-------------^_^.^_^.^_^.^_^.^_^-----

----登录
--function onLogin(page,this)
--page:stop(this)
--  local account=getElementValue(page,"accountID")
--  local password=getElementValue(page,"passwordID")
--  if account == "" or account == nil then
--    page:showToast("请输入账号")
--  return
--  end
--  
--  if password == "" or password == nil then
--    page:showToast("请输入密码")
--  return
--  end
--
--  page:getActivity():showProgreesDialog("正在登录")
--  local Parameters={
--    NetSelector="NetData:",                
--    URL="/client/EAPLoginCheck.do",
--    LuaNetCallBack="onLoginNetCallBack(page,{response})",
--    LuaExceptionCallBack="ExceptionCallBack(page)",
--    RequestParams={channel="",loginID=account,password=password},
--    Method="POST",
--    ResponseType="JSON",    
----    Message="正在登录"
--  }
--  page:request(Parameters);
--end
--
--function onLoginNetCallBack(page,response)
--  page:log("*************************"..response["resultCode"])
--  page:log(response["response"])
--  page:getActivity():hideProgreesDialog()
--  if response["resultCode"] == 0 then
--    page:putShareHeadler("cookie",response["headers"]["Set-Cookie"])
--    saveUserDetail(page)
--    page:getActivity():deleteSCSSAlbum()
--        
--    local hall=response["response"]["data"]["WayName"]    
--    local today=response["response"]["data"]["ServerTime"]   
--    local week=response["response"]["data"]["WeekDay"]    
--    local bundle=page:initBundle()
--      bundle:putString("hall",hall)    
--      bundle:putString("today",today)    
--      bundle:putString("week",week)    
--      page:setCustomAnimations("left")
--      page:nextPage("res://homePage.xml",true,bundle)
--  else
--    page:showToast(response["resultMsg"])
--  end
--end
--
--function ExceptionCallBack(page)
--  page:getActivity():hideProgreesDialog()
--end


-----^_^.^_^.^_^.^_^.^_^-------------生产-------------^_^.^_^.^_^.^_^.^_^-----

--提交并获取验证码（登录）
function onLogin(page,this)
--this:stop(this)
  local account=getElementValue(page,"accountID")
  local password=getElementValue(page,"passwordID")
  if account == "" or account == nil then
    page:showToast("请输入账号")
  return
  end
  
  if password == "" or password == nil then
    page:showToast("请输入密码")
  return
  end
  
  local fLoginName = page:getActivity():encryptCs(account)
  local p = page:getActivity():encryptCs(password)
  
  page:getActivity():showProgreesDialog("正在获取")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/EAPLoginCheck4.do",
    LuaNetCallBack="onLoginNetCallBack(page,{response})",
    LuaExceptionCallBack="onLoginExceptionCallBack(page)",
    RequestParams={channel="",loginID=fLoginName,pwd=p},
    Method="POST",
    ResponseType="JSON",    
--    Message="正在获取"
  }
  page:request(Parameters);
end

function onLoginNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  page:getActivity():hideProgreesDialog()
  if response["resultCode"] == 0 then
    page:putShareHeadler("cookie",response["headers"]["Set-Cookie"])
      
    setElementHidden(page,"group_loginID",true)
    setElementHidden(page,"group_smsID",false)
    
    --倒计时
    nextCountDown=60
    onCountDown(page)
    smsButton:setDisable(true)
        
    saveUserDetail(page) --保存and清空数据
    page:getActivity():deleteSCSSAlbum() --清空拍照签名图片
  else
    page:showToast(response["resultMsg"])
  end
end

function onLoginExceptionCallBack(page)
  page:showToast("网络异常！")
  page:getActivity():hideProgreesDialog()
end

--重新获取验证码
function onObtain(page)
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/sendMsg2.do",
    LuaNetCallBack="onObtainNetCallBack(page,{response})",
    LuaExceptionCallBack="onObtainExceptionCallBack(page)",
    RequestParams={},
    Method="POST",
    ResponseType="JSON",    
    Message="正在获取"
  }
  page:request(Parameters);
end

function onObtainNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    --倒计时
    nextCountDown=60
    onCountDown(page)
    smsButton:setDisable(true)
    page:showToast("短信验证码获取成功")
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
    smsButton:setValue(nextCountDown.."秒")
    page:postDelayed("onCountDown(page)",1000)
  else
    nextCountDown=0
    smsButton:setValue("重新获取")
    smsButton:setDisable(false)
  end
end

--登录（校验短信验证码）
function onSMSCheck(page)
  local sms=getElementValue(page,"smsID")
  if sms == "" or sms == nil then
    page:showToast("请输入短信验证码")
  return
  end
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/validateContent.do",
    LuaNetCallBack="onSMSCheckNetCallBack(page,{response})",
    LuaExceptionCallBack="onSMSCheckExceptionCallBack(page)",
    RequestParams={vcode=sms},
    Method="POST",
    ResponseType="JSON",    
    Message="正在登录"
  }
  page:request(Parameters);
end

function onSMSCheckNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local hall=response["response"]["data"]["WayName"]    
    local today=response["response"]["data"]["ServerTime"]   
    local week=response["response"]["data"]["WeekDay"]    
    local updateTitle=response["response"]["data"]["Title"]    
    local updateContent=response["response"]["data"]["Detail"]    
    local updateVersion=response["response"]["data"]["APPVersion"]    
    local bundle=page:initBundle()
      bundle:putString("hall",hall)    
      bundle:putString("today",today)    
      bundle:putString("week",week)    
      bundle:putString("updateTitle",updateTitle)    
      bundle:putString("updateContent",updateContent)    
      bundle:putString("updateVersion",updateVersion)    
      page:setCustomAnimations("left")
      page:nextPage("res://homePage.xml",true,bundle)
  else
    page:showToast(response["resultMsg"])
  end
end

function onSMSCheckExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--重新输入账号/密码
function onAgain(page)
  nextCountDown=0
  setElementHidden(page,"group_loginID",false)
  setElementHidden(page,"group_smsID",true)
end

function saveUserDetail(page)
  local edit=userInfoSP:edit()
  edit:putString("account",page:getActivity():encrypt(getElementValue(page,"accountID")))
  if isRemember == true then
    edit:putString("password",page:getActivity():encrypt(getElementValue(page,"passwordID")))
  elseif isRemember == false then
    edit:putString("password","")
  end
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
  edit:putString("deviceConnect",page:getActivity():encrypt("false"))
  edit:commit()
end

function onBackPressed(page)
  page:getActivity():exit()
end