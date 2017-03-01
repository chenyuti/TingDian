require ("public")
local userInfoSP
local mobileNum --客户电话号码
local recordType --选择个人or单位

local personalDocumentType --个人申请证件类型对应的编号
local companyDocumentType --单位申请-单位信息证件类型对应的编号
local managersDocumentType --单位申请-经办人信息证件类型对应的编号
local userDocumentType --单位申请-使用/责任人信息证件类型对应的编号
local personalCard --个人用户-证件类型，IDCard为身份证，other为其他
local unitCard --单位用户-经办人信息-证件类型，IDCard为身份证，other为其他
local userCard --单位用户-使用/责任人信息-证件类型，IDCard为身份证，other为其他

local picturePath=""

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  
  customerDetail(page)
end


-----^_^.^_^.^_^.^_^.^_^-------------小左边-------------^_^.^_^.^_^.^_^.^_^-----

--获取账户信息
function customerDetail(page)
  mobileNum=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
  local customerBrand=page:getActivity():decrypt(userInfoSP:getString("brand",""))
  local customerRealName=page:getActivity():decrypt(userInfoSP:getString("realName",""))
  local customerCardType=page:getActivity():decrypt(userInfoSP:getString("cardType",""))
  
    setElementValue(page,"customer_mobileID",mobileNum)
    setElementValue(page,"customer_brandID",customerBrand)
    
    --已实名登记or已身份确认
    if customerRealName == "000" or customerRealName == "1" then
      --已身份确认
      if customerRealName == "000" then
        setElementValue(page,"customer_realNameID","已身份确认")
        --个人
        if customerCardType == "IdCard" or customerCardType == "TaiBaoZheng" or customerCardType == "SoldierID" or customerCardType == "PolicePaper" or customerCardType == "Passport" or customerCardType == "HuKouBu" or customerCardType == "HKIdCard" then
          setElementSelected(page,"btn_personalID",true)
          setElementHidden(page,"btn_unitID",true)
        --单位
        elseif customerCardType == "CorpLicence" or customerCardType == "BusinessLicence" or customerCardType == "UnitID" or customerCardType == "OrgaLicence" or customerCardType == "OrgCodeCard" then
          setElementSelected(page,"btn_unitID",true)
          setElementHidden(page,"btn_personalID",true)
        end
        setElementHidden(page,"group_passID",false)
        
      --已实名登记
      elseif customerRealName == "1" then
        setElementValue(page,"customer_realNameID","已实名登记")
        setElementSelected(page,"btn_personalID",true)
        setElementSelected(page,"btn_unitID",false)
              
        setElementHidden(page,"group_applyID",false)
        setElementHidden(page,"group_personalID",false)
        setElementHidden(page,"group_unitID",true)
        setElementEnable(page,"btn_personal_submitID",true)
        setElementEnable(page,"btn_unit_submitID",true)
        recordType="personal"
        personalCard="IDCard"
        unitCard="IDCard"      
        userCard="IDCard"      
      end
      
    --未实名登记
    else
      setElementValue(page,"customer_realNameID","未实名登记")
      setElementSelected(page,"btn_personalID",true)
      setElementSelected(page,"btn_unitID",false)
            
      setElementHidden(page,"group_applyID",false)
      setElementHidden(page,"group_personalID",false)
      setElementHidden(page,"group_unitID",true)
      setElementEnable(page,"btn_personal_submitID",true)
      setElementEnable(page,"btn_unit_submitID",true)
      recordType="personal"
      personalCard="IDCard"
      unitCard="IDCard"      
      userCard="IDCard"      
    end
end

--登记类型——个人or单位
function onRecord(page,this,type)
  if getElementValue(page,"customer_realNameID") == "未实名登记" or getElementValue(page,"customer_realNameID") == "已实名登记" then
    if type == "personal" then
      setElementSelected(page,"btn_personalID",true)      
      setElementSelected(page,"btn_unitID",false)
      setElementHidden(page,"group_personalID",false)
      setElementHidden(page,"group_unitID",true)      
      recordType="personal"
      if personalCard == "IDCard" then
        if getElementValue(page,"personal_idcard_numberID") == "请使用读卡器读取" or getElementValue(page,"personal_idcard_nameID") == "" or getElementValue(page,"personal_idcard_addressID") == "" then    
          setElementEnable(page,"btn_personal_submitID",true)
        else
          setElementEnable(page,"btn_personal_submitID",false)
        end      
      elseif personalCard == "other" then
        if getElementValue(page,"personal_other_numberID") == "" or getElementValue(page,"personal_other_nameID") == "" or getElementValue(page,"personal_other_addressID") == "" then    
          setElementEnable(page,"btn_personal_submitID",true)
        else
          setElementEnable(page,"btn_personal_submitID",false)
        end      
      end
    elseif type == "unit" then
      setElementSelected(page,"btn_personalID",false)      
      setElementSelected(page,"btn_unitID",true)
      setElementHidden(page,"group_personalID",true)
      setElementHidden(page,"group_unitID",false)      
      recordType="unit"
      if unitCard == "IDCard" and userCard == "IDCard" then
        if getElementValue(page,"company_numberID") == "" or getElementValue(page,"company_nameID") == "" or getElementValue(page,"company_addressID") == "" or getElementValue(page,"unit_idcard_numberID") == "请使用读卡器读取" or getElementValue(page,"unit_idcard_nameID") == "" or getElementValue(page,"unit_idcard_addressID") == "" or getElementValue(page,"user_idcard_numberID") == "请使用读卡器读取" or getElementValue(page,"user_idcard_nameID") == "" or getElementValue(page,"user_idcard_addressID") == "" then    
          setElementEnable(page,"btn_unit_submitID",true)
        else
          setElementEnable(page,"btn_unit_submitID",false)
        end      
      elseif unitCard == "IDCard" and userCard == "other" then
        if getElementValue(page,"company_numberID") == "" or getElementValue(page,"company_nameID") == "" or getElementValue(page,"company_addressID") == "" or getElementValue(page,"unit_idcard_numberID") == "请使用读卡器读取" or getElementValue(page,"unit_idcard_nameID") == "" or getElementValue(page,"unit_idcard_addressID") == "" or getElementValue(page,"user_other_numberID") == "" or getElementValue(page,"user_other_nameID") == "" or getElementValue(page,"user_other_addressID") == "" then    
          setElementEnable(page,"btn_unit_submitID",true)
        else
          setElementEnable(page,"btn_unit_submitID",false)
        end      
      elseif unitCard == "other" and userCard == "IDCard" then
        if getElementValue(page,"company_numberID") == "" or getElementValue(page,"company_nameID") == "" or getElementValue(page,"company_addressID") == "" or getElementValue(page,"unit_other_numberID") == "" or getElementValue(page,"unit_other_nameID") == "" or getElementValue(page,"unit_other_addressID") == "" or getElementValue(page,"user_idcard_numberID") == "请使用读卡器读取" or getElementValue(page,"user_idcard_nameID") == "" or getElementValue(page,"user_idcard_addressID") == "" then    
          setElementEnable(page,"btn_unit_submitID",true)
        else
          setElementEnable(page,"btn_unit_submitID",false)
        end      
      elseif unitCard == "other" and userCard == "other" then
        if getElementValue(page,"company_numberID") == "" or getElementValue(page,"company_nameID") == "" or getElementValue(page,"company_addressID") == "" or getElementValue(page,"unit_other_numberID") == "" or getElementValue(page,"unit_other_nameID") == "" or getElementValue(page,"unit_other_addressID") == "" or getElementValue(page,"user_other_numberID") == "" or getElementValue(page,"user_other_nameID") == "" or getElementValue(page,"user_other_addressID") == "" then    
          setElementEnable(page,"btn_unit_submitID",true)
        else
          setElementEnable(page,"btn_unit_submitID",false)
        end      
      end      
    end
  end
end

-----^_^.^_^.^_^.^_^.^_^-------------实名登记步骤1-------------^_^.^_^.^_^.^_^.^_^-----

function onValueChange(page,this,type)
  if type == "personal" then
    if getElementValue(page,"personal_documentID") == "身份证" then
      setElementHidden(page,"group_personal_IDCardID",false)
      setElementHidden(page,"group_personal_otherID",true)
      personalCard="IDCard"
    else
      setElementHidden(page,"group_personal_IDCardID",true)
      setElementHidden(page,"group_personal_otherID",false)
      personalCard="other"
    end
  elseif type == "unit" then
    if getElementValue(page,"unit_documentID") == "身份证" then
      setElementHidden(page,"group_unit_IDCardID",false)
      setElementHidden(page,"group_unit_otherID",true)
      unitCard="IDCard"
    else
      setElementHidden(page,"group_unit_IDCardID",true)
      setElementHidden(page,"group_unit_otherID",false)
      unitCard="other"
    end
  elseif type == "user" then
    if getElementValue(page,"user_documentID") == "身份证" then
      setElementHidden(page,"group_user_IDCardID",false)
      setElementHidden(page,"group_user_otherID",true)
      userCard="IDCard"
    else
      setElementHidden(page,"group_user_IDCardID",true)
      setElementHidden(page,"group_user_otherID",false)
      userCard="other"
    end
  end
end

--扫描身份证信息
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
  
  if scanningCard == "personal" then
    setElementValue(page,"personal_idcard_numberID",card[3])
    setElementValue(page,"personal_idcard_nameID",card[1])
    setElementValue(page,"personal_idcard_addressID",card[2])
    setElementEnable(page,"btn_personal_submitID",false)
  elseif scanningCard == "unit" then
    setElementValue(page,"unit_idcard_numberID",card[3])
    setElementValue(page,"unit_idcard_nameID",card[1])
    setElementValue(page,"unit_idcard_addressID",card[2])
    if userCard == "IDCard" then
      if getElementValue(page,"company_numberID") == "" or getElementValue(page,"company_nameID") == "" or getElementValue(page,"company_addressID") == "" or getElementValue(page,"user_idcard_numberID") == "请使用读卡器读取" or getElementValue(page,"user_idcard_nameID") == "" or getElementValue(page,"user_idcard_addressID") == "" then    
        setElementEnable(page,"btn_unit_submitID",true)
      else
        setElementEnable(page,"btn_unit_submitID",false)
      end      
    elseif userCard == "other" then
      if getElementValue(page,"company_numberID") == "" or getElementValue(page,"company_nameID") == "" or getElementValue(page,"company_addressID") == "" or getElementValue(page,"user_other_numberID") == "" or getElementValue(page,"user_other_nameID") == "" or getElementValue(page,"user_other_addressID") == "" then    
        setElementEnable(page,"btn_unit_submitID",true)
      else
        setElementEnable(page,"btn_unit_submitID",false)
      end     
    end     
  elseif scanningCard == "user" then
    setElementValue(page,"user_idcard_numberID",card[3])
    setElementValue(page,"user_idcard_nameID",card[1])
    setElementValue(page,"user_idcard_addressID",card[2])
    if unitCard == "IDCard" then
      if getElementValue(page,"company_numberID") == "" or getElementValue(page,"company_nameID") == "" or getElementValue(page,"company_addressID") == "" or getElementValue(page,"unit_idcard_numberID") == "请使用读卡器读取" or getElementValue(page,"unit_idcard_nameID") == "" or getElementValue(page,"unit_idcard_addressID") == "" then    
        setElementEnable(page,"btn_unit_submitID",true)
      else
        setElementEnable(page,"btn_unit_submitID",false)
      end      
    elseif unitCard == "other" then
      if getElementValue(page,"company_numberID") == "" or getElementValue(page,"company_nameID") == "" or getElementValue(page,"company_addressID") == "" or getElementValue(page,"unit_other_numberID") == "" or getElementValue(page,"unit_other_nameID") == "" or getElementValue(page,"unit_other_addressID") == "" then    
        setElementEnable(page,"btn_unit_submitID",true)
      else
        setElementEnable(page,"btn_unit_submitID",false)
      end    
    end    
  end
end

function onTextPersonal(page)
  if getElementValue(page,"personal_other_numberID") == "" or getElementValue(page,"personal_other_nameID") == "" or getElementValue(page,"personal_other_addressID") == "" then    
    setElementEnable(page,"btn_personal_submitID",true)
  else
    setElementEnable(page,"btn_personal_submitID",false)
  end 
end

function onTextUnit(page)
  if unitCard == "IDCard" and userCard == "IDCard" then
    if getElementValue(page,"company_numberID") == "" or getElementValue(page,"company_nameID") == "" or getElementValue(page,"company_addressID") == "" or getElementValue(page,"unit_idcard_numberID") == "请使用读卡器读取" or getElementValue(page,"unit_idcard_nameID") == "" or getElementValue(page,"unit_idcard_addressID") == "" or getElementValue(page,"user_idcard_numberID") == "请使用读卡器读取" or getElementValue(page,"user_idcard_nameID") == "" or getElementValue(page,"user_idcard_addressID") == "" then    
      setElementEnable(page,"btn_unit_submitID",true)
    else
      setElementEnable(page,"btn_unit_submitID",false)
    end      
  elseif unitCard == "IDCard" and userCard == "other" then
    if getElementValue(page,"company_numberID") == "" or getElementValue(page,"company_nameID") == "" or getElementValue(page,"company_addressID") == "" or getElementValue(page,"unit_idcard_numberID") == "请使用读卡器读取" or getElementValue(page,"unit_idcard_nameID") == "" or getElementValue(page,"unit_idcard_addressID") == "" or getElementValue(page,"user_other_numberID") == "" or getElementValue(page,"user_other_nameID") == "" or getElementValue(page,"user_other_addressID") == "" then    
      setElementEnable(page,"btn_unit_submitID",true)
    else
      setElementEnable(page,"btn_unit_submitID",false)
    end      
  elseif unitCard == "other" and userCard == "IDCard" then
    if getElementValue(page,"company_numberID") == "" or getElementValue(page,"company_nameID") == "" or getElementValue(page,"company_addressID") == "" or getElementValue(page,"unit_other_numberID") == "" or getElementValue(page,"unit_other_nameID") == "" or getElementValue(page,"unit_other_addressID") == "" or getElementValue(page,"user_idcard_numberID") == "请使用读卡器读取" or getElementValue(page,"user_idcard_nameID") == "" or getElementValue(page,"user_idcard_addressID") == "" then    
      setElementEnable(page,"btn_unit_submitID",true)
    else
      setElementEnable(page,"btn_unit_submitID",false)
    end      
  elseif unitCard == "other" and userCard == "other" then
    if getElementValue(page,"company_numberID") == "" or getElementValue(page,"company_nameID") == "" or getElementValue(page,"company_addressID") == "" or getElementValue(page,"unit_other_numberID") == "" or getElementValue(page,"unit_other_nameID") == "" or getElementValue(page,"unit_other_addressID") == "" or getElementValue(page,"user_other_numberID") == "" or getElementValue(page,"user_other_nameID") == "" or getElementValue(page,"user_other_addressID") == "" then    
      setElementEnable(page,"btn_unit_submitID",true)
    else
      setElementEnable(page,"btn_unit_submitID",false)
    end      
  end 
end

--申请实名登记步骤1-下一步
local checkCardID
local checkDocumentType
function onSubmitSure(page,type)
  if type == "personal" then
    isDocumentType(page,getElementValue(page,"personal_documentID"),"check")
    if personalCard == "IDCard" then
      checkCardID=getElementValue(page,"personal_idcard_numberID")
    elseif personalCard == "other" then
      checkCardID=getElementValue(page,"personal_other_numberID")
    end
  elseif type == "unit" then
    isDocumentType(page,getElementValue(page,"user_documentID"),"check")
    if userCard == "IDCard" then
      checkCardID=getElementValue(page,"user_idcard_numberID")
    elseif userCard == "other" then
      checkCardID=getElementValue(page,"user_other_numberID")  
    end
  end
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/account/queryOneCertMulMachine.do",
    LuaNetCallBack="CheckDecomentNetCallBack(page,{response})",
    LuaExceptionCallBack="CheckDecomentExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",certId=checkCardID,certType=checkDocumentType},
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
    local checkNum=response["response"]["data"]["Count"]
    if tonumber(checkNum) >= 5 then
      setElementValue(page,"fail_wordID","您使用的身份证号已登记5个账号，\n请重新选择证件号码。")
      setElementHidden(page,"group_applyID",true)
      setElementHidden(page,"group_failID",false)     
    else
      setElementHidden(page,"group_applyID",true)
      setElementHidden(page,"group_apply2ID",false)
      
      if recordType == "personal" then
        setElementSelected(page,"btn_personalID",true)
        setElementHidden(page,"btn_unitID",true)   
      elseif recordType == "unit" then
        setElementSelected(page,"btn_unitID",true)
        setElementHidden(page,"btn_personalID",true)
      end
      onStartImageResult(page)
    end
  elseif response["resultCode"] == 1 and response["response"]["data"]["Count"] == "" then
    setElementHidden(page,"group_applyID",true)
    setElementHidden(page,"group_apply2ID",false)
    
    if recordType == "personal" then
      setElementSelected(page,"btn_personalID",true)
      setElementHidden(page,"btn_unitID",true)   
    elseif recordType == "unit" then
      setElementSelected(page,"btn_unitID",true)
      setElementHidden(page,"btn_personalID",true)
    end
    onStartImageResult(page)
  else
    page:showToast(response["resultMsg"])
  end
end

function CheckDecomentExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

local documentValue
function isDocumentType(page,value,type)
  if value == "身份证" then
    documentValue="IdCard"
  elseif value == "台胞证" then
    documentValue="TaiBaoZheng"
  elseif value == "军人证" then
    documentValue="SoldierID"
  elseif value == "警官证" then
    documentValue="PolicePaper"
  elseif value == "护照" then
    documentValue="Passport"
  elseif value == "户口簿" then
    documentValue="HuKouBu"
  elseif value == "港澳证" then
    documentValue="HKIdCard"
  elseif value == "事业单位法人证书" then
    documentValue="CorpLicence"
  elseif value == "营业执照" then
    documentValue="BusinessLicence"
  elseif value == "单位证明" then
    documentValue="UnitID"
  elseif value == "社会团体法人登记证书" then
    documentValue="OrgaLicence"
  elseif value == "组织机构代码证" then
    documentValue="OrgCodeCard"
  end

  if type == "personal" then
    personalDocumentType=documentValue
  elseif type == "company" then
    companyDocumentType=documentValue
  elseif type == "managers" then
    managersDocumentType=documentValue
  elseif type == "user" then
    userDocumentType=documentValue
  elseif type == "check" then
    checkDocumentType=documentValue
  end  
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


-----^_^.^_^.^_^.^_^.^_^-------------实名登记步骤2-------------^_^.^_^.^_^.^_^.^_^-----

--业务单详情
function onContractDetail(page)
  local bundle=page:initBundle()
    bundle:putString("protocol","realName")
    bundle:putString("signatureURL",getElementValue(page,"signature_urlID"))
    if recordType == "personal" then
      bundle:putString("businessType","realNameResignPrivate")
      bundle:putString("userCardType","")
      bundle:putString("userName","")
      bundle:putString("userAddress","")
      bundle:putString("userCardNum","")
      bundle:putString("managerCardType","")
      bundle:putString("managerName","")
      bundle:putString("managerAddress","")
      bundle:putString("managerCardNum","")
        
      bundle:putString("newCardType",getElementValue(page,"personal_documentID"))
      if personalCard == "IDCard" then
        bundle:putString("newName",getElementValue(page,"personal_idcard_nameID"))
        bundle:putString("newAddress",getElementValue(page,"personal_idcard_addressID"))
        bundle:putString("newCardNum",getElementValue(page,"personal_idcard_numberID"))
      elseif personalCard == "other" then
        bundle:putString("newName",getElementValue(page,"personal_other_nameID"))
        bundle:putString("newAddress",getElementValue(page,"personal_other_addressID"))
        bundle:putString("newCardNum",getElementValue(page,"personal_other_numberID"))
      end
    elseif recordType == "unit" then
      bundle:putString("businessType","realNameResignPublic")
      bundle:putString("newCardType",getElementValue(page,"user_documentID"))
      bundle:putString("userCardType",getElementValue(page,"user_documentID"))
      
      if userCard == "IDCard" then
        bundle:putString("newName",getElementValue(page,"user_idcard_nameID"))
        bundle:putString("newAddress",getElementValue(page,"user_idcard_addressID"))
        bundle:putString("newCardNum",getElementValue(page,"user_idcard_numberID"))
        
        bundle:putString("userName",getElementValue(page,"user_idcard_nameID"))
        bundle:putString("userAddress",getElementValue(page,"user_idcard_addressID"))
        bundle:putString("userCardNum",getElementValue(page,"user_idcard_numberID"))
      elseif userCard == "other" then
        bundle:putString("newName",getElementValue(page,"user_other_nameID"))
        bundle:putString("newAddress",getElementValue(page,"user_other_addressID"))
        bundle:putString("newCardNum",getElementValue(page,"user_other_numberID"))
        
        bundle:putString("userName",getElementValue(page,"user_other_nameID"))
        bundle:putString("userAddress",getElementValue(page,"user_other_addressID"))
        bundle:putString("userCardNum",getElementValue(page,"user_other_numberID"))
      end
      
      bundle:putString("managerCardType",getElementValue(page,"unit_documentID"))
      if unitCard == "IDCard" then
        bundle:putString("managerName",getElementValue(page,"unit_idcard_nameID"))
        bundle:putString("managerAddress",getElementValue(page,"unit_idcard_addressID"))
        bundle:putString("managerCardNum",getElementValue(page,"unit_idcard_numberID"))
      elseif unitCard == "other" then
        bundle:putString("managerName",getElementValue(page,"unit_other_nameID"))
        bundle:putString("managerAddress",getElementValue(page,"unit_other_addressID"))
        bundle:putString("managerCardNum",getElementValue(page,"unit_other_numberID"))
      end      
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
    if recordType == "personal" then
      page:getActivity():photograph(mobileNum,"ResignPrivate")
    elseif recordType == "unit" then
      page:getActivity():photograph(mobileNum,"ResignPublic")
    end  
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
local isUploadFail=false
function onSubmit(page)
  local signaturePath=page:getArguments():getString("signaturePath")
  if signaturePath == "" or signaturePath == nil then
    page:showToast("请先签名")
    return
  end
  
  setElementEnable(page,"btn_submitID",true)
  
  onImageUpload(page)
--  if isUploadFail then
--    onImageUpload(page)
--  else
--    local customerRealName=page:getActivity():decrypt(userInfoSP:getString("realName",""))
--    if customerRealName == "1" then
--      onRemoveRealName(page)
--    else
--      onImageUpload(page)
--    end
--  end
end

--二次登记先退登记
function onRemoveRealName(page)
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/regstatusmodify.do",
    LuaNetCallBack="onRemoveRealNameNetCallBack(page,{response})",
    LuaExceptionCallBack="onRemoveRealNameExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum},
    Method="POST",
    ResponseType="JSON",    
    Message="正在办理"
  }
  page:request(Parameters);
end

function onRemoveRealNameNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local edit=userInfoSP:edit()
      edit:putString("realName","")
      edit:putString("userName","")
      edit:putString("userNum","")
      edit:putString("userAddress","")
      edit:commit()
    onImageUpload(page)    
  else  
    setElementValue(page,"fail_wordID",response["resultMsg"])
    setElementHidden(page,"group_apply2ID",true)
    setElementHidden(page,"group_failID",false)
  end
end

function onRemoveRealNameExceptionCallBack(page)
  setElementEnable(page,"btn_submitID",false)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function onImageUpload(page)
  local signaturePath=page:getArguments():getString("signaturePath")
  local path=split(picturePath,",")
  local size=table.getn(path)
  local postFilePaths=getImagePostFilePaths(page,signaturePath,path,size)  
  local remark=getElementValue(page,"remark_ID")
  page:getActivity():showProgreesDialog("正在提交")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/imgsUpload.do",
    LuaNetCallBack="SubmitNetCallBack(page,{response})",
    LuaExceptionCallBack="SubmitExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,businessType="实名登记",remark=remark},
    PostFilePaths=postFilePaths,
    Method="FORM",
    ResponseType="JSON",    
--    Message="正在提交"
  }
  page:request(Parameters);
end

function SubmitNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  page:getActivity():hideProgreesDialog()
  if response["resultCode"] == 0 then
    isUploadFail=false
    if recordType == "personal" then
      onPersonalSubmit(page)
    elseif recordType == "unit" then
      onUnitSubmit(page)
    end
  else
    isUploadFail=true
    setElementEnable(page,"btn_submitID",false)
    page:showToast("图片上传失败")
  end
end

function SubmitExceptionCallBack(page)
  isUploadFail=true
  setElementEnable(page,"btn_submitID",false)
  page:showToast("网络异常！")
  page:getActivity():hideProgreesDialog()
end

local idcardNum=""
local idcardName=""
local idcardAddress=""
local delegateId=""
local delegateName=""
local delegateAddr=""
function onPersonalSubmit(page)
  isDocumentType(page,getElementValue(page,"personal_documentID"),"personal")
  if personalCard == "IDCard" then
    idcardNum=getElementValue(page,"personal_idcard_numberID")
    idcardName=getElementValue(page,"personal_idcard_nameID")
    idcardAddress=getElementValue(page,"personal_idcard_addressID")
  elseif personalCard == "other" then
    idcardNum=getElementValue(page,"personal_other_numberID")
    idcardName=getElementValue(page,"personal_other_nameID")
    idcardAddress=getElementValue(page,"personal_other_addressID")
  end

  page:getActivity():showProgreesDialog("正在办理")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/realIdentityRegiste.do",
    LuaNetCallBack="SubmitSureNetCallBack(page,{response})",
    LuaExceptionCallBack="SubmitSureExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",IP="",certType=personalDocumentType,certNo=idcardNum,certAddress=idcardAddress,customerName=idcardName,source="2",officeTel="",delegateName="",delegateIdType="",delegateId="",delegateAddr="",delegatePhone="",dutyUserName="",dutyGender="",dutyCertId="",dutyCertType="",dutyCertAddr=""},
    Method="POST",
    ResponseType="JSON",    
--    Message="正在办理"
  }
  page:request(Parameters);
end

local dutyCertId=""
local dutyUserName=""
local dutyCertAddr=""
function onUnitSubmit(page)
  isDocumentType(page,getElementValue(page,"company_documentID"),"company")
  isDocumentType(page,getElementValue(page,"unit_documentID"),"managers")
  isDocumentType(page,getElementValue(page,"user_documentID"),"user")
  idcardNum=getElementValue(page,"company_numberID")
  idcardName=getElementValue(page,"company_nameID")
  idcardAddress=getElementValue(page,"company_addressID")
  if unitCard == "IDCard" then
    delegateId=getElementValue(page,"unit_idcard_numberID")
    delegateName=getElementValue(page,"unit_idcard_nameID")
    delegateAddr=getElementValue(page,"unit_idcard_addressID")
  elseif unitCard == "other" then
    delegateId=getElementValue(page,"unit_other_numberID")
    delegateName=getElementValue(page,"unit_other_nameID")
    delegateAddr=getElementValue(page,"unit_other_addressID")
  end
  
  if userCard == "IDCard" then
    dutyCertId=getElementValue(page,"user_idcard_numberID")
    dutyUserName=getElementValue(page,"user_idcard_nameID")
    dutyCertAddr=getElementValue(page,"user_idcard_addressID")
  elseif userCard == "other" then
    dutyCertId=getElementValue(page,"user_other_numberID")
    dutyUserName=getElementValue(page,"user_other_nameID")
    dutyCertAddr=getElementValue(page,"user_other_addressID")
  end
  
  page:getActivity():showProgreesDialog("正在办理")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/realIdentityRegiste.do",
    LuaNetCallBack="SubmitSureNetCallBack(page,{response})",
    LuaExceptionCallBack="SubmitSureExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",IP="",certType=companyDocumentType,certNo=idcardNum,certAddress=idcardAddress,customerName=idcardName,source="2",officeTel="",delegateName=delegateName,delegateIdType=managersDocumentType,delegateId=delegateId,delegateAddr=delegateAddr,delegatePhone="",dutyUserName=dutyUserName,dutyGender="",dutyCertId=dutyCertId,dutyCertType=userDocumentType,dutyCertAddr=dutyCertAddr},
    Method="POST",
    ResponseType="JSON",    
--    Message="正在办理"
  }
  page:request(Parameters);
end

function SubmitSureNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  page:getActivity():hideProgreesDialog()
  if response["resultCode"] == 0 then
    setElementHidden(page,"group_apply2ID",true)
    setElementHidden(page,"group_passID",false)
    setElementValue(page,"customer_realNameID","已实名登记")
    local edit=userInfoSP:edit()
      edit:putString("realName",page:getActivity():encrypt("1"))
      if recordType == "personal" then
        edit:putString("userName",page:getActivity():encrypt(idcardName))
        edit:putString("userNum",page:getActivity():encrypt(idcardNum))
        edit:putString("userAddress",page:getActivity():encrypt(idcardAddress))
      elseif recordType == "unit" then
        edit:putString("userName",page:getActivity():encrypt(dutyUserName))
        edit:putString("userNum",page:getActivity():encrypt(dutyCertId))
        edit:putString("userAddress",page:getActivity():encrypt(dutyCertAddr))
      end
      edit:commit()  
      --倒计时
      onCountDown(page,"btn_realNameID")  
  else
    setElementValue(page,"fail_wordID",response["resultMsg"])
    setElementHidden(page,"group_apply2ID",true)
    setElementHidden(page,"group_failID",false)    
  end
end

function SubmitSureExceptionCallBack(page)
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

--重新进行实名登记
function onAgain(page)
  setElementSelected(page,"btn_personalID",true)
  setElementSelected(page,"btn_unitID",false)      
  setElementHidden(page,"btn_personalID",false)
  setElementHidden(page,"btn_unitID",false)
  
  setElementHidden(page,"group_applyID",false)
  setElementHidden(page,"group_personalID",false)
  setElementHidden(page,"group_unitID",true)
  setElementEnable(page,"btn_personal_submitID",true)
  setElementEnable(page,"btn_unit_submitID",true)
  
  setElementHidden(page,"group_apply2ID",true)
  setElementHidden(page,"group_passID",true)
  setElementHidden(page,"group_failID",true)
  
  setElementEnable(page,"btn_submitID",false)
  
  recordType="personal"
  personalCard="IDCard"
  unitCard="IDCard"  
  userCard="IDCard"  
end

function goBack(page)
  page:setCustomAnimations("right")
  page:goBack()  
end

function onBackPressed(page)
  goBack(page)
end