require ("public")
local userInfoSP
local popupModelView
local reasonPopupView
local documentTypeValue="IdCard" --证件类型对应的编号
local IDCardNameView --身份证-客户姓名
local IDCardSexView --身份证-性别
local IDCardNumberView --身份证-证件号码
local IDCardAddressView --身份证-证件地址
local otherNameView --其他-客户姓名
local otherSexView --其他-性别
local otherNumberView --其他-证件号码
local otherAddressView --其他-证件地址
local contactNameView --联系人姓名
local contactMobileView --联系电话
local contactAddressView --客户地址
local firstPasswordView --服务密码
local secondPasswordView --确认密码
local startNowButton --立即生效按钮
local meal38Button --主套餐38元按钮
local picturePath=""

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  popupModelView=page:getElementById("pop_modelView")
  reasonPopupView=page:getElementById("reason_popupID")
  
  IDCardNameView=page:getElementById("idcard_nameID")
  IDCardSexView=page:getElementById("idcard_sexID")
  IDCardNumberView=page:getElementById("idcard_numberID")
  IDCardAddressView=page:getElementById("idcard_addressID")
  otherNameView=page:getElementById("other_nameID")
  otherSexView=page:getElementById("other_sexID")
  otherNumberView=page:getElementById("other_numberID")
  otherAddressView=page:getElementById("other_addressID")
  contactNameView=page:getElementById("contact_nameID")
  contactMobileView=page:getElementById("contact_mobileID")
  contactAddressView=page:getElementById("contact_addressID")
  firstPasswordView=page:getElementById("first_pwdID")
  secondPasswordView=page:getElementById("second_pwdID")  
  startNowButton=page:getElementById("btn_startNowID")  
  meal38Button=page:getElementById("btn_meal38ID")  
  
  setElementSelected(page,"btn_apply1ID",true)
end


-----^_^.^_^.^_^.^_^.^_^-------------大右边-------------^_^.^_^.^_^.^_^.^_^-----

--大右边返回
function onLastBack(page,step)
  onLeftButton(page)
  if step == "2" then
    setElementSelected(page,"btn_apply1ID",true)
    setElementHidden(page,"group_apply1ID",false)    
    setElementHidden(page,"group_apply2ID",true)    
  elseif step == "3" then
    setElementSelected(page,"btn_apply2ID",true)
    setElementHidden(page,"group_apply2ID",false)    
    setElementHidden(page,"group_apply3ID",true)    
  elseif step == "5" then
    setElementSelected(page,"btn_apply3ID",true)
    setElementHidden(page,"group_apply3ID",false)    
    setElementHidden(page,"group_apply5ID",true)    
  end
end

--下一步
function onNextStep(page,step)
  if step == "1" then
    onApplyDecoment(page)
    onStartSelected(page,startNowButton,"now")  
    onFlyMeal(page,meal38Button,"prod.10086000003043")  
  elseif step == "2" then
    onLeftButton(page)
    setElementSelected(page,"btn_apply3ID",true)
    setElementHidden(page,"group_apply2ID",true)    
    setElementHidden(page,"group_apply3ID",false)    
  elseif step == "3" then
    onGenerateOrder(page)   
  end
end

function onLeftButton(page)
  setElementSelected(page,"btn_apply1ID",false)
  setElementSelected(page,"btn_apply2ID",false)
  setElementSelected(page,"btn_apply3ID",false)
  setElementSelected(page,"btn_apply5ID",false)
end


-----^_^.^_^.^_^.^_^.^_^-------------个人客户开户-------------^_^.^_^.^_^.^_^.^_^-----

local customerCard="IDCard" --证件类型，IDCard为身份证，other为其他
function onValueChange(page,this)
  if getElementValue(page,"customer_documentID") == "身份证" then
    setElementHidden(page,"group_IDCardID",false)
    setElementHidden(page,"group_otherID",true)
    customerCard="IDCard"
  else
    setElementHidden(page,"group_IDCardID",true)
    setElementHidden(page,"group_otherID",false)
    customerCard="other"
  end
end

--扫描身份证信息
function onBluetoothScanning(page,type)
  local bluetoothDevice=page:getActivity():decrypt(userInfoSP:getString("bluetoothDevice",""))
  local deviceConnect=page:getActivity():decrypt(userInfoSP:getString("deviceConnect",""))
  if bluetoothDevice == "" then
    page:setCustomAnimations("left")
    page:nextPage("res://devicePage.xml",false,nil)
  return  
  end
  
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
  
  setElementValue(page,"idcard_nameID",card[1])
  setElementValue(page,"idcard_sexID",card[4])
  setElementValue(page,"idcard_numberID",card[3])
  setElementValue(page,"idcard_addressID",card[2])
end

--校验
local checkCardID
local arrearsNum=0
local isCheck=false
function onCheckDecoment(page)
  selectedDocumentType(page,getElementValue(page,"customer_documentID"))
  if customerCard == "IDCard" then
    checkCardID=IDCardNumberView:getValue()
  elseif customerCard == "other" then
    checkCardID=otherNumberView:getValue()
  end
  
  if checkCardID == "" then
    page:showToast("请输入证件号码")
  return
  end
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/account/queryOneCertMulMachine.do",
    LuaNetCallBack="CheckDecomentNetCallBack(page,{response})",
    LuaExceptionCallBack="CheckDecomentExceptionCallBack(page)",
    RequestParams={phoneNum="",channel="",certId=checkCardID,certType=documentTypeValue},
    Method="POST",
    ResponseType="JSON",     
    Message="正在校验"
  }
  page:request(Parameters);
end

function CheckDecomentNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    arrearsNum=response["response"]["data"]["Count"]
    isCheck=true
    onArrearsList(page)
  elseif response["resultCode"] == 1 and response["response"]["data"]["Count"] == "" then
    arrearsNum=0
    isCheck=true
    setElementValue(page,"check_imageID","res://icon_circle_yes.png")
    setElementValue(page,"check_wordID","该证件登记未满5户且未欠费")
    setElementHidden(page,"group_checkID",false)
  else
    page:showToast(response["resultMsg"])
  end
end

function CheckDecomentExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function onArrearsList(page)
  page:getActivity():showProgreesDialog("正在校验")
  local Parameters={
    NetSelector="NetData:",                
    URL="/account/queryCustomerOverdueInfo.do",
    LuaNetCallBack="ArrearsListNetCallBack(page,{response})",
    LuaExceptionCallBack="ArrearsListExceptionCallBack(page)",
    RequestParams={phoneNum="",channel="",certId=checkCardID,certType=documentTypeValue},
    Method="POST",
    ResponseType="JSON",     
--    Message="正在校验"
  }
  page:request(Parameters);
end

function ArrearsListNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  page:getActivity():hideProgreesDialog()
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    if tonumber(arrearsNum) == 5 then
      setElementValue(page,"check_imageID","res://icon_circle_no.png")
      setElementValue(page,"check_wordID","该证件登记已满5户，欠费情况如下：")
    elseif tonumber(arrearsNum) < 5 then
      if #data == 0 then
        setElementValue(page,"check_imageID","res://icon_circle_yes.png")
        setElementValue(page,"check_wordID","该证件登记未满5户且未欠费")
      else
        setElementValue(page,"check_imageID","res://icon_circle_yes.png")
        setElementValue(page,"check_wordID","该证件登记未满5户，欠费情况如下：")
      end
    end
    local listView=page:getElementById("check_listID")
    listView:getEntity():getTemplateData():clear()
    local entityData={}
    for i=1,#data,1 do
      entityData[i]={subEntityValues={account_detailID=data[i]["PhoneNumber"].."欠费: ￥"..data[i]["Fee"]/100}}
    end
    listView:upDateEntity(entityData)    
    setElementHidden(page,"group_checkID",false)
  else
    page:showToast(response["resultMsg"])
  end
end

function ArrearsListExceptionCallBack(page)
  page:showToast("网络异常！")
  page:getActivity():hideProgreesDialog()
--  page:hideProgreesDialog()
end

--粘贴
function onPaste(page)
  contactNameView:clearText()
  contactAddressView:clearText()
  if getElementValue(page,"customer_documentID") == "身份证" then
    setElementValue(page,"contact_nameID",getElementValue(page,"idcard_nameID"))
    setElementValue(page,"contact_addressID",getElementValue(page,"idcard_addressID"))
  else
    setElementValue(page,"contact_nameID",getElementValue(page,"other_nameID"))
    setElementValue(page,"contact_addressID",getElementValue(page,"other_addressID"))
  end
end

--清除数据
function onClearDecoment(page)
  IDCardNameView:setValue("")
  IDCardSexView:setValue("")
  IDCardNumberView:setValue("")
  IDCardAddressView:setValue("")
  otherNameView:clearText()
  otherSexView:clearText()
  otherNumberView:clearText()
  otherAddressView:clearText()
  contactNameView:clearText()
  contactMobileView:clearText()
  contactAddressView:clearText()
  firstPasswordView:clearText()
  secondPasswordView:clearText()
end

local passwordWord
function onTextChanged(page,this,type)
  local first=getElementValue(page,"first_pwdID")
  local second=getElementValue(page,"second_pwdID")
  if type == "first" then
    passwordWord=first
  elseif type == "second" then
    passwordWord=second
  end
  
  if (passwordWord == "000000" or passwordWord == "111111" or passwordWord == "222222" or passwordWord == "333333" or passwordWord == "444444" or passwordWord == "555555" or passwordWord == "666666" or passwordWord == "7777777" or passwordWord == "888888" or passwordWord == "999999" or passwordWord == "012345" or passwordWord == "123456" or passwordWord == "234567" or passwordWord == "345678" or passwordWord == "456789" or passwordWord == "567890" or passwordWord == "098765" or passwordWord == "987654" or passwordWord == "876543" or passwordWord == "765432" or passwordWord == "654321" or passwordWord == "543210") and passwordWord ~= "" then
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

local custName=""
local gender=""
local certId=""
local certAddress=""
function onApplyDecoment(page)
  if customerCard == "IDCard" then
    if IDCardNameView:getValue() == "" or IDCardSexView:getValue() == "" or IDCardNumberView:getValue() == "" or IDCardAddressView:getValue() == "" or contactNameView:getValue() == "" or contactMobileView:getValue() == "" or contactAddressView:getValue() == "" or firstPasswordView:getValue() == "" or secondPasswordView:getValue() == "" then 
      page:showToast("请把开户信息填充完整")
    return
    end    
  elseif customerCard == "other" then
    if otherNameView:getValue() == "" or otherSexView:getValue() == "" or otherNumberView:getValue() == "" or otherAddressView:getValue() == "" or contactNameView:getValue() == "" or contactMobileView:getValue() == "" or contactAddressView:getValue() == "" or firstPasswordView:getValue() == "" or secondPasswordView:getValue() == "" then 
      page:showToast("请把开户信息填充完整")
    return
    end    
  end
  
  if firstPasswordView:getValue() ~= secondPasswordView:getValue() then
    page:showToast("两个密码不一致")
  return
  end
  
  if isCheck == false then
    page:showToast("请先校验")
  return
  end     

  selectedDocumentType(page,getElementValue(page,"customer_documentID"))
  if customerCard == "IDCard" then
    custName=IDCardNameView:getValue()
    certId=IDCardNumberView:getValue()
    certAddress=IDCardAddressView:getValue()
    if IDCardSexView:getValue() == "男" then
      gender="1"
    elseif IDCardSexView:getValue() == "女" then
      gender="0"
    end
  elseif customerCard == "other" then
    custName=otherNameView:getValue()
    certId=otherNumberView:getValue()
    certAddress=otherAddressView:getValue()
    if otherSexView:getValue() == "男" then
      gender="1"
    elseif otherSexView:getValue() == "女" then
      gender="0"
    end
  end
  local linkMan=contactNameView:getValue()
  local linkPhone=contactMobileView:getValue()
  local address=contactAddressView:getValue()
  local Parameters={
    NetSelector="NetData:",                
    URL="/account/openAccount.do",
    LuaNetCallBack="ApplyDecomentNetCallBack(page,{response})",
    LuaExceptionCallBack="ApplyDecomentExceptionCallBack(page)",
    RequestParams={phoneNum="",channel="",IP="",userAccount="",custName=custName,certType=documentTypeValue,certId=certId,certAddress=certAddress,linkMan=linkMan,linkPhone=linkPhone,address=address,shortName="",gender=gender,linkAddress=address},
    Method="POST",
    ResponseType="JSON",     
    Message="正在校验"
  }
  page:request(Parameters);
end

local custID
function ApplyDecomentNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    custID=response["response"]["data"]["CustID"]
    onLeftButton(page)
    setElementSelected(page,"btn_apply2ID",true)
    setElementHidden(page,"group_apply1ID",true)    
    setElementHidden(page,"group_apply2ID",false)
    onModelList(page)   
  else
    page:showToast(response["resultMsg"])
  end
end

function ApplyDecomentExceptionCallBack(page)
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
end


-----^_^.^_^.^_^.^_^.^_^-------------选择主体产品与套餐-------------^_^.^_^.^_^.^_^.^_^-----

--获取付费模式列表
function onModelList(page)
  local Parameters={
    NetSelector="NetData:",                
    URL="/account/queryPayTypeInfo.do",
    LuaNetCallBack="ModelListNetCallBack(page,{response})",
    LuaExceptionCallBack="ModelListExceptionCallBack(page)",
    RequestParams={phoneNum="",channel="",IP="",productId="0"},
    Method="POST",
    ResponseType="JSON",     
    Message="正在加载"
  }
  page:request(Parameters);
end

function ModelListNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    setElementValue(page,"model_ID",""..data[1]["PayType"])
    setElementValue(page,"model_nameID",data[1]["PayTypeName"])
    
    local listView=page:getElementById("model_listID")
    listView:getEntity():getTemplateData():clear()
    local entityData={}
    for i=1,#data,1 do
      entityData[i]={subEntityValues={spinner_ID=data[i]["PayType"],spinner_nameID=data[i]["PayTypeName"]}}
    end  
    listView:upDateEntity(entityData)
  else
    page:showToast(response["resultMsg"])
  end
end

function ModelListExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function onModelView(page)
  popupModelView:showInFragment("bottom",0,0)  
end

--选择付费模式
function onSpinnerSelected(page,this)
  setElementValue(page,"model_ID",this:getElementById("spinner_ID"):getValue())
  setElementValue(page,"model_nameID",this:getElementById("spinner_nameID"):getValue())
  popupModelView:dismiss()
end

--选择生效时间
local lastStart
local startTime --选择的生效时间，立即生效=2，下月生效=3
function onStartSelected(page,this,type)
  if lastStart ~= nil and lastStart ~= this then
    lastStart:setSelected(false)
  end
  lastStart=this
  this:setSelected(true) 
  if type == "now" then
    startTime="2"
  elseif type == "next" then
    startTime="3"
  end
end

--套餐选择
local flyID
local lastFly
function onFlyMeal(page,this,id)
  flyID=id
  if lastFly ~= nil and lastFly ~= this then
    lastFly:setSelected(false)
  end
  lastFly=this
  this:setSelected(true)  
end


-----^_^.^_^.^_^.^_^.^_^-------------号卡选择-------------^_^.^_^.^_^.^_^.^_^-----

--选号
function onSelectedMobile(page)
  page:setCustomAnimations("left")
  page:nextPage("res://pickingNumberPage.xml",false,nil)
end

--扫描SIM卡条形码
function onSweep(page)
  page:getActivity():sweepBarCode()
end

--SIM卡扫描结果
function sweepResult(page,data)
  setElementValue(page,"card_simID",data:getExtras():getString("result"))
end

--获取IMSI
function getIMSI(page)
  local mobile=getElementValue(page,"selected_mobileID")
  if mobile == "" or mobile == nil then
    page:showToast("请选择手机号码")
  return
  end
  
  local sim=getElementValue(page,"card_simID")
  if sim == "" or sim == nil then
    page:showToast("请输入SIM卡号")
  return
  end
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/account/queryIMSIInfo.do",
    LuaNetCallBack="IMSINetCallBack(page,{response})",
    LuaExceptionCallBack="IMSIExceptionCallBack(page)",
    RequestParams={phoneNum=mobile,channel="",cardId=sim,puk=""},
    Method="POST",
    ResponseType="JSON",     
    Message="正在获取"
  }
  page:request(Parameters);  
end

function IMSINetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    setElementValue(page,"card_imsiID",data["EntityInfo"])
  else
    page:showToast(response["resultMsg"])
  end
end

function IMSIExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--号卡选择下一步--生成订单
local subName
function onGenerateOrder(page)
  local contactMobile=getElementValue(page,"contact_mobileID")
  local mobile=getElementValue(page,"selected_mobileID")
  local mobilePrice=getElementValue(page,"selected_mobilePriceID")
  local sim=getElementValue(page,"card_simID")
  local imsi=getElementValue(page,"card_imsiID")
  local payMode=getElementValue(page,"model_ID")
  if mobile == "" or mobile == nil or sim == "" or sim == nil or imsi == "" or imsi == nil then
    page:showToast("请填写三个信息")
  return
  end
  
  if customerCard == "IDCard" then
    subName=IDCardNameView:getValue()
  elseif customerCard == "other" then
    subName=otherNameView:getValue()
  end  
  local password=firstPasswordView:getValue()
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/account/openAccountOrderCheck.do",
    LuaNetCallBack="GenerateOrderNetCallBack(page,{response})",
    LuaExceptionCallBack="GenerateOrderExceptionCallBack(page)",
    RequestParams={phoneNum=contactMobile,channel="",custID=custID,custType="1",IMSIValue=imsi,newNum=mobile,newNumPrice=mobilePrice,newSimNum=sim,fourGEffType=startTime,fourGMainProdId=flyID,subName=subName,pwd=password,payMode=payMode,authType="AuthCheckB"},
    Method="POST",
    ResponseType="JSON",     
    Message="正在获取"
  }
  page:request(Parameters);  
end

function GenerateOrderNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    setElementValue(page,"pay_orderID",data["OrderSeq"])
    setElementValue(page,"pay_discountID","￥"..data["DeratePrice"])
    setElementValue(page,"pay_actualID","￥"..(data["Fee"]/100))
    setElementSelected(page,"pay_posID",true)
    onStartImageResult(page)
    
    onLeftButton(page)    
    setElementSelected(page,"btn_apply5ID",true)
    setElementHidden(page,"group_apply3ID",true)    
    setElementHidden(page,"group_apply5ID",false)     
  else
    page:showToast(response["resultMsg"])
  end
end

function GenerateOrderExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
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


-------^_^.^_^.^_^.^_^.^_^-------------选择营销方案-------------^_^.^_^.^_^.^_^.^_^-----
--
--
----删除
--function onSchemeCancle(page,this)
--
--
--end
--
----筛选
--function onScreen(page)
--  local schemeNum=getElementValue(page,"scheme_numID")
--  if schemeNum == "" or schemeNum == nil then
--    page:showToast("请输入编码")
--  return
--  end
--  
--  local schemeName=getElementValue(page,"scheme_nameID")
--  if schemeName == "" or schemeName == nil then
--    page:showToast("请输入名称")
--  return
--  end
--
--
--end
--
----查看全部
--function onLookAll(page)
--
--
--end
--
----选择
--function onSchemeSelected(page,this)
--
--
--end


-----^_^.^_^.^_^.^_^.^_^-------------完成开户-------------^_^.^_^.^_^.^_^.^_^-----

--选择POS机or现金
local paymentSelected="pos" --支付方式，posPOS机，cash现金
function onPaymentType(page,this,type)
  paymentSelected=type
  setElementSelected(page,"pay_posID",false)
  setElementSelected(page,"pay_cashID",false)
  if type == "pos" then
    setElementSelected(page,"pay_posID",true)
    setElementHidden(page,"group_isPOSID",false)
  elseif type == "cash" then
    setElementSelected(page,"pay_cashID",true)
    setElementHidden(page,"group_isPOSID",true)
  end
end

--业务单详情
function onContractDetail(page)
  local selectedMobile=getElementValue(page,"selected_mobileID")
  local discount=string.sub(getElementValue(page,"pay_discountID"),4,string.len(getElementValue(page,"pay_discountID")))
  local actual=string.sub(getElementValue(page,"pay_actualID"),4,string.len(getElementValue(page,"pay_actualID")))
  local model=getElementValue(page,"model_nameID")
  local bundle=page:initBundle()
    bundle:putString("protocol","openAccount")
    bundle:putString("signatureURL",getElementValue(page,"signature_urlID"))
    bundle:putString("selectedMobile",selectedMobile)
    bundle:putString("discount",discount)
    bundle:putString("actual",actual)
    bundle:putString("model",model)
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
    RequestParams={pages="2"},
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
    setElementValue(page,"write_signatureID","local://"..signaturePath)
    setElementValue(page,"signature_urlID",signaturePath)
    setElementEnable(page,"btn_printID",false)
  end
  
  local selectedMobile=page:getArguments():getString("selectedMobile")
  local selectedPrice=page:getArguments():getString("selectedPrice")
  if selectedMobile ~= getElementValue(page,"selected_mobileID") and selectedMobile ~= nil then
    setElementValue(page,"selected_mobileID",selectedMobile)
    setElementValue(page,"selected_mobilePriceID",selectedPrice)
  end  
end

--拍照
function onPhotograph(page,this)
  local isPhotograph=this:getElementById("isPhotographID"):getValue()
  if isPhotograph == "photograph" then
    local selectedMobile=getElementValue(page,"selected_mobileID")
    page:getActivity():photograph(selectedMobile,"OpenAccount")
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
  local signaturePath=page:getArguments():getString("signaturePath")
  if signaturePath == "" or signaturePath == nil then
    page:showToast("请先签名")
    return
  end
  
  local posNum=getElementValue(page,"pos_numID")
  if paymentSelected == "pos" and posNum == "" then
    page:showToast("请输入POS单号")
  return
  end  
  setElementEnable(page,"btn_submitID",true)
  
  local path=split(picturePath,",")
  local size=table.getn(path)
  local postFilePaths=getImagePostFilePaths(page,signaturePath,path,size)  
  local remark=getElementValue(page,"remark_ID")
  local mobileNum=getElementValue(page,"selected_mobileID")
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/imgsUpload.do",
    LuaNetCallBack="SubmitNetCallBack(page,{response})",
    LuaExceptionCallBack="SubmitExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,businessType="全球通开户",remark=remark},
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

local payType
function onBusinessHandle(page)
  local contactMobile=getElementValue(page,"contact_mobileID")
  local phoneNum=getElementValue(page,"selected_mobileID")
  local orderSeq=getElementValue(page,"pay_orderID")
  local fee=getElementValue(page,"pay_actualID")
  local newFee=string.sub(fee,4,string.len(fee))
  
  local paySeq
  if paymentSelected == "pos" then
    payType="5"
    paySeq=getElementValue(page,"pos_numID")
  elseif paymentSelected == "cash" then
    payType="1"
    paySeq=""
  end
  
  page:getActivity():showProgreesDialog("正在办理")
  local Parameters={
    NetSelector="NetData:",                
    URL="/account/openAccountCommitOrder.do",
    LuaNetCallBack="BusinessHandleNetCallBack(page,{response})",
    LuaExceptionCallBack="BusinessHandleExceptionCallBack(page)",
    RequestParams={phoneNum=contactMobile,channel="",orderSeq=orderSeq,payType=payType,fee=""..newFee*100,payMode="",payOrg="",paySeq="",custId=custID,custType="1"},
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
    setElementEnable(page,"btn_submitID",false)
    setElementValue(page,"pass_wordID","您已成功开户！\nIMSI号码为："..getElementValue(page,"card_imsiID"))
    setElementHidden(page,"group_apply5ID",true)
    setElementHidden(page,"group_passID",false)
    --倒计时
    onCountDown(page,"btn_openAccountID")
    setElementValue(page,"order_NumID",response["response"]["data"]["OrderSeq"])
  else
    setElementValue(page,"fail_wordID",response["resultMsg"])
    setElementHidden(page,"group_apply5ID",true)
    setElementHidden(page,"group_failID",false)
  end
end

function BusinessHandleExceptionCallBack(page)
  setElementEnable(page,"btn_submitID",false)
  page:showToast("网络异常！")
  page:getActivity():hideProgreesDialog()
--  page:hideProgreesDialog()
end


-----^_^.^_^.^_^.^_^.^_^-------------成功or失败-------------^_^.^_^.^_^.^_^.^_^-----

--本次服务已完成
function onCustomerQuit(page)
  onCustomerLoginQuit(page)
end

--一键回退
function onFallback(page)
  reasonPopupView:showInFragment("center",0,0)
end

function onFallbackSure(page)
  reasonPopupView:dismiss()
  local phoneNum=getElementValue(page,"selected_mobileID")
  local orderNum=getElementValue(page,"order_NumID")
  local reason=getElementValue(page,"reasonID")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/cancelOrder.do",
    LuaNetCallBack="FallbackNetCallBack(page,{response})",
    LuaExceptionCallBack="FallbackExceptionCallBack(page)",
    RequestParams={phoneNum=phoneNum,channel="",IP="",orderSeq=orderNum,reason=reason,payType="1"},
    Method="POST",
    ResponseType="JSON",    
    Message="正在回退"
  }
  page:request(Parameters);
end

function FallbackNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    setElementHidden(page,"group_apply1ID",false)
    setElementHidden(page,"group_passID",true)
    page:showToast("回退成功")
  else
    page:showToast(response["resultMsg"])
  end
end

function FallbackExceptionCallBack(page)
  page:hideProgreesDialog()
end

function onAgain(page)
  setElementEnable(page,"btn_submitID",false)
  setElementHidden(page,"group_apply5ID",false)
  setElementHidden(page,"group_failID",true)
end

function goBack(page)
  page:setCustomAnimations("right")
  page:goBack()  
end

function onBackPressed(page)
  goBack(page)
end