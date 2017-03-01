require ("public")
local userInfoSP
local IDCardNameView
local IDCardNumView
local IDCardAddressView
local otherNameView
local otherNumView
local otherAddressView

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  IDCardNameView=page:getElementById("idcard_nameID")
  IDCardNumView=page:getElementById("idcard_numID")
  IDCardAddressView=page:getElementById("idcard_addressID")
  otherNameView=page:getElementById("other_nameID")
  otherNumView=page:getElementById("other_numID")
  otherAddressView=page:getElementById("other_addressID")
  
  local customerUserName=page:getActivity():decrypt(userInfoSP:getString("userName",""))
  local customerCardType=page:getActivity():decrypt(userInfoSP:getString("cardType",""))
  local customerUserNum=page:getActivity():decrypt(userInfoSP:getString("userNum",""))
  local customerUserAddress=page:getActivity():decrypt(userInfoSP:getString("userAddress",""))
  
  if customerCardType ~= "" then
    local cardType=onCardType(page,customerCardType)
    setElementValue(page,"selected_validID",cardType)
      
    if customerCardType == "IdCard" then
      IDCardNameView:setValue(customerUserName)
      IDCardNumView:setValue(customerUserNum)
      IDCardAddressView:setValue(customerUserAddress)
      setElementHidden(page,"group_idcardID",false)
      setElementHidden(page,"group_otherID",true)
    else
      otherNameView:setValue(customerUserName)
      otherNumView:setValue(customerUserNum)
      otherAddressView:setValue(customerUserAddress)
      setElementHidden(page,"group_idcardID",true)
      setElementHidden(page,"group_otherID",false)    
    end  
  end
end

local cardValue
function onCardType(page,value)
  if value == "IdCard" then
    cardValue="身份证"
  elseif value == "SoldierID" then
    cardValue="士兵证"
  elseif value == "PolicePaper" then
    cardValue="警官证"
  elseif value == "Passport" then
    cardValue="护照"
  elseif value == "HuKouBu" then
    cardValue="户口簿"
  elseif value == "HKIdCard" then
    cardValue="港澳证"
  elseif value == "IdVipCard" then
    cardValue="VIP卡"
  elseif value == "GongZhang" then
    cardValue="公章"
  elseif value == "TempId" then
    cardValue="临时身份证"
  elseif value == "DriverIC" then
    cardValue="驾驶证"
  elseif value == "PLA" then
    cardValue="军官证"
  elseif value == "IdTypeJSX" then
    cardValue="单位介绍信"
  elseif value == "StudentID" then
    cardValue="学生证"
  elseif value == "TeacherID" then
    cardValue="教师证"
  elseif value == "FuYuanZheng" then
    cardValue="复员证"
  elseif value == "ZanZhuZheng" then
    cardValue="暂住证"
  elseif value == "RetNative" then
    cardValue="还乡证"
  elseif value == "OtherLicence" then
    cardValue="其他"
  elseif value == "BusinessLicence" then
    cardValue="营业执照"
  elseif value == "CorpLicence" then
    cardValue="事业单位法人证书"
  elseif value == "UnitID" then
    cardValue="单位证明"
  elseif value == "OrgaLicence" then
    cardValue="社会团体法人登记证书"
  end
  return cardValue
end

--清除重填
function onClear(page)
  IDCardNameView:setValue("")
  IDCardNumView:setValue("")
  IDCardAddressView:setValue("")
  otherNameView:clearText()
  otherNumView:clearText()
  otherAddressView:clearText()
end

function onValueChange(page,this)
  local selectedValid=getElementValue(page,"selected_validID")
  if selectedValid == "身份证" then
    setElementHidden(page,"group_idcardID",false)
    setElementHidden(page,"group_otherID",true)
  else
    setElementHidden(page,"group_idcardID",true)
    setElementHidden(page,"group_otherID",false)
  end
end

--读卡器读取
function onBluetoothScanning(page)
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
  setElementValue(page,"idcard_numID",card[3])
  setElementValue(page,"idcard_addressID",card[2])
end

local managersName
local managersNum
local managersAddress
function onSubmitSure(page,type)
  local managersType=getElementValue(page,"selected_validID")
  if type == "idcard" then
    managersName=IDCardNameView:getValue()
    managersNum=IDCardNumView:getValue()
    managersAddress=IDCardAddressView:getValue()
  elseif type == "other" then
    managersName=otherNameView:getValue()
    managersNum=otherNumView:getValue()
    managersAddress=otherAddressView:getValue()
  end
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/login/handlemanResign.do",
    LuaNetCallBack="SubmitSureNetCallBack(page,{response})",
    LuaExceptionCallBack="SubmitSureExceptionCallBack(page)",
    RequestParams={handleman=managersName,handlemanCardType=managersType,handlemanCardNo=managersNum,handlemanAddr=managersAddress},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters)
end

function SubmitSureNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local protocol=page:getArguments():getString("protocol")
    if protocol == "4G" then
      local signatureURL=page:getArguments():getString("signatureURL")
      local businessType=page:getArguments():getString("businessType")
      local discount=page:getArguments():getString("discount")
      local actual=page:getArguments():getString("actual")
      local commodityCode=page:getArguments():getString("commodityCode")
      local bundle=page:initBundle()
        bundle:putString("protocol","4G")
        bundle:putString("signatureURL",signatureURL)
        bundle:putString("businessType",businessType)
        bundle:putString("discount",""..discount)
        bundle:putString("actual",""..actual)
        bundle:putString("commodityCode",commodityCode)
        page:setCustomAnimations("left")
        page:nextPage("res://protocolPage.xml",true,bundle)
    
    elseif protocol == "payment" then
      local signatureURL=page:getArguments():getString("signatureURL")
      local actual=page:getArguments():getString("actual")
      local bundle=page:initBundle()
        bundle:putString("protocol","payment")
        bundle:putString("signatureURL",signatureURL)
        bundle:putString("actual",actual)
        page:setCustomAnimations("left")
        page:nextPage("res://protocolPage.xml",true,bundle)
        
    elseif protocol == "replacementCard" then
      local signatureURL=page:getArguments():getString("signatureURL")
      local businessType=page:getArguments():getString("businessType")
      local discount=page:getArguments():getString("discount")
      local actual=page:getArguments():getString("actual")
      local sim=page:getArguments():getString("sim")
      local bundle=page:initBundle()
        bundle:putString("protocol","replacementCard")
        bundle:putString("signatureURL",signatureURL)
        bundle:putString("businessType",businessType)
        bundle:putString("discount",discount)
        bundle:putString("actual",actual)
        bundle:putString("sim",sim)        
        page:setCustomAnimations("left")
        page:nextPage("res://protocolPage.xml",true,bundle)
      
    elseif protocol == "broadband" then
      local signatureURL=page:getArguments():getString("signatureURL")
      local discount=page:getArguments():getString("discount")
      local actual=page:getArguments():getString("actual")
      local homeBroadbandType=page:getArguments():getString("homeBroadbandType")
      local planID=page:getArguments():getString("planID")
      local bundle=page:initBundle()
        bundle:putString("protocol","broadband")
        bundle:putString("signatureURL",signatureURL)
        bundle:putString("discount",discount)
        bundle:putString("actual",actual)
        bundle:putString("homeBroadbandType",homeBroadbandType)        
        bundle:putString("planID",planID)        
        page:setCustomAnimations("left")
        page:nextPage("res://protocolPage.xml",true,bundle)
      
    elseif protocol == "flowYear" then
      local signatureURL=page:getArguments():getString("signatureURL")
      local planID=page:getArguments():getString("planID")
      local plan2ID=page:getArguments():getString("plan2ID")
      local bundle=page:initBundle()
        bundle:putString("protocol","flowYear")
        bundle:putString("signatureURL",signatureURL)
        bundle:putString("planID",planID)        
        bundle:putString("plan2ID",plan2ID)        
        page:setCustomAnimations("left")
        page:nextPage("res://protocolPage.xml",true,bundle)
    end
         
    local edit=userInfoSP:edit()
    edit:putString("managers",page:getActivity():encrypt("true"))
    edit:commit()    
  else  
    page:showToast(response["resultMsg"])
  end
end

function SubmitSureExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function selectedDocumentType(page,value)
  local validValue
  if value == "身份证" then
    validValue="IdCard"
  elseif value == "士兵证" then
    validValue="SoldierID"
  elseif value == "警官证" then
    validValue="PolicePaper"
  elseif value == "护照" then
    validValue="Passport"
  elseif value == "户口簿" then
    validValue="HuKouBu"
  elseif value == "港澳证" then
    validValue="HKIdCard"
  elseif value == "VIP卡" then
    validValue="IdVipCard"
  elseif value == "公章" then
    validValue="GongZhang"
  elseif value == "临时身份证" then
    validValue="TempId"
  elseif value == "驾驶证" then
    validValue="DriverIC"
  elseif value == "军官证" then
    validValue="PLA"
  elseif value == "单位介绍信" then
    validValue="IdTypeJSX"
  elseif value == "学生证" then
    validValue="StudentID"
  elseif value == "教师证" then
    validValue="TeacherID"
  elseif value == "复员证" then
    validValue="FuYuanZheng"
  elseif value == "暂住证" then
    validValue="ZanZhuZheng"
  elseif value == "还乡证" then
    validValue="RetNative"
  elseif value == "其他" then
    validValue="OtherLicence"
  elseif value == "营业执照" then
    validValue="BusinessLicence"
  elseif value == "事业单位法人证书" then
    validValue="CorpLicence"
  elseif value == "单位证明" then
    validValue="UnitID"
  elseif value == "社会团体法人登记证书" then
    validValue="OrgaLicence"    
  end
  return validValue  
end

function goBack(page)
  page:setCustomAnimations("right")
  page:goBack()  
end

function onBackPressed(page)
  goBack(page)
end