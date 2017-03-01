require ("public")
local userInfoSP
local mobileNum
local IDCardButton
local freeButton
local charge --是否免费，free免费，notFree不免费
local posButton
local picturePath=""

function PageInit(page) 
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  IDCardButton=page:getElementById("btn_IDCardID")
  freeButton=page:getElementById("btn_freeID")
  posButton=page:getElementById("btn_posID")
  
  customerDetail(page)
end


-----^_^.^_^.^_^.^_^.^_^-------------小左边-------------^_^.^_^.^_^.^_^.^_^-----

--获取账户信息
local LoginType
local customerRealName
function customerDetail(page)
  mobileNum=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
  local customerLoginType=page:getActivity():decrypt(userInfoSP:getString("loginType",""))
  local customerBrand=page:getActivity():decrypt(userInfoSP:getString("brand",""))
  customerRealName =page:getActivity():decrypt(userInfoSP:getString("realName",""))
  local customerUSIMCard=page:getActivity():decrypt(userInfoSP:getString("usimCard",""))
	LoginType = page:getActivity():decrypt(userInfoSP:getString("LoginType",""))
    setElementValue(page,"customer_mobileID",mobileNum)
    setElementValue(page,"customer_brandID",customerBrand)

    if customerRealName == "000" or customerRealName == "1" then
      setElementValue(page,"customer_realNameID","已实名登记")
    else
      setElementValue(page,"customer_realNameID","未实名登记")
    end
    
    if customerLoginType == "sms" then
      setElementHidden(page,"group_loginID",true)
      setElementHidden(page,"group_callLogsID",true)
      setElementValue(page,"btn_handleID","换卡")
      setElementHidden(page,"btn_stepID",false)
    elseif customerLoginType == "service" then
      setElementValue(page,"btn_handleID","补卡")
      if customerRealName == "000" then
        setElementHidden(page,"group_loginID",false)
        setElementHidden(page,"group_callLogsID",true)
        onLoginSelected(page,IDCardButton,"IDCard")
      elseif customerRealName == "1" then
        setElementHidden(page,"group_loginID",true)
        setElementHidden(page,"group_callLogsID",false)
        --设置提示语
        setRemind(page)
        --设置提示语
      else
        setElementHidden(page,"group_loginID",true)
        setElementHidden(page,"group_callLogsID",false)
        --设置提示语
        setRemind(page)
        --设置提示语
      end
    elseif customerLoginType == "IDCard" or customerLoginType == "valid" then
      setElementValue(page,"btn_handleID","补卡")
      setElementHidden(page,"group_loginID",true)
      setElementHidden(page,"group_callLogsID",true)
      setElementHidden(page,"btn_stepID",false)
    end     

    if string.find(customerUSIMCard,"USIM") ~= nil then
      setElementValue(page,"old_cardID","USIM卡")
    else
      setElementValue(page,"old_cardID","SIM卡")
      
      onChargeSelected(page,freeButton,"free")
      setElementHidden(page,"btn_notFreeID",true)
    end    
end

--设置通讯录提示语
function setRemind(page)
	--设置提示语
    setElementHidden(page,"group_scan_card",true)
    setElementHidden(page,"group_scan_submit",true)
    setElementHidden(page,"group_const_submit",false)
    --设置提示语
end

local nextLogin
local loginSelected
function onLoginSelected(page,this,type)
  if nextLogin ~= nil and nextLogin ~= this then
    nextLogin:setSelected(false)
  end
  nextLogin=this
  this:setSelected(true)
  
  if type == "IDCard" then
    isIdCard = true
    setElementHidden(page,"group_validID",true)
    setElementHidden(page,"group_idcardID",false)
  elseif type == "valid" then
    isIdCard = false
    setElementHidden(page,"group_validID",false)
    setElementHidden(page,"group_idcardID",true)
    setElementHidden(page,"group_scan_submit",true)
    setElementHidden(page,"group_const_submit",true)
  end
  loginSelected=type
end

function onValueChange(page,this)
  local selectedDocument=getElementValue(page,"selected_validID")
  if selectedDocument == "身份证" then
    isIdCard = true
    setElementHidden(page,"group_validIDCardID",false)
    setElementHidden(page,"group_validOtherID",true)
  else
    isIdCard = false
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

local IdCardReadSuccess = false --读卡是否成功
function ScanningResult(page)
	IdCardReadSuccess = true
	
	--设置提示语
	setElementHidden(page,"group_scan_submit",false)
	setElementHidden(page,"group_scan_card",true)
	--设置提示语
	
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

local validValueID="IdCard" --证件类型对应的编号
local isIdCard = true
function onLoginSure(page,this)
  if loginSelected == "IDCard" then
    isIdCard = true;
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
    isIdCard = true;
      validNum=getElementValue(page,"valid_idcardNumID")
      if validNum == "" or validNum == nil then
        page:showToast("请读取证件号码")
        return
      end 
    else
      validNum=getElementValue(page,"valid_otherNumID")
      isIdCard = false;
      if validNum == "" or validNum == nil then
        page:showToast("请输入证件号码")
        return
      end 
    end   
  
    validSelectedValue(page,getElementValue(page,"selected_validID"))
    local verType
    if selectedValid == "身份证" then
    isIdCard = true;
      verType="AuthCheckE"
    else
    isIdCard = false;
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
    this:stop(this)
  end   
end

function LoginNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if isIdCard == true then
    if response["resultCode"] == 0 then
      setElementHidden(page,"group_loginID",true)
      setElementHidden(page,"btn_stepID",false)
      local edit=userInfoSP:edit()
        edit:putString("loginType",page:getActivity():encrypt(loginSelected))
        edit:commit()    
    else
      page:showToast(response["resultMsg"])
    end
    return;
  end
  if isIdCard == false then
    if response["resultCode"] == 0 then
      setElementHidden(page,"group_loginID",true)
      
      if customerRealName == "000" or customerRealName == "1" or LoginType == "randompwd" then
      setElementHidden(page,"group_callLogsID",true)
      else
      setElementHidden(page,"group_callLogsID",false)
      end
      
       --设置提示语
       setRemind(page)
       --设置提示语
       local edit=userInfoSP:edit()
        edit:putString("loginType",page:getActivity():encrypt(loginSelected))
        edit:commit()    
    else
      page:showToast(response["resultMsg"])
  --onCallLogsSure(page)
    end
  end
end

function LoginExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function onCallLogsSure(page,this)
  local callNum1=getElementValue(page,"call_num1ID")
  local callNum2=getElementValue(page,"call_num2ID")
  local callNum3=getElementValue(page,"call_num3ID")
  if callNum1 == "" or callNum1 == nil or callNum2 == "" or callNum2 == nil or callNum3 == "" or callNum3 == nil then
    page:showToast("请输入手机号码")
  return
  end
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/backup/queryChkNumberVoiceInfo.do",
    LuaNetCallBack="CallLogsNetCallBack(page,{response})",
    LuaExceptionCallBack="CallLogsExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",type="1",mobileNum1=callNum1,mobileNum2=callNum2,mobileNum3=callNum3},
    Method="POST",
    ResponseType="JSON",     
    Message="正在验证"
  }
  this:stop(this)
  page:request(Parameters);
end

function CallLogsNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    if #data == 0 then
    setNumNoticeVisible(page,0,0,0)
      page:showToast("三个号码都未通过验证")
    else
      if data[1]["Result"] == 1 and data[2]["Result"] == 1 and data[3]["Result"] == 1 then
        setElementHidden(page,"group_callLogsID",true)
        setElementHidden(page,"group_scan_card",true)
        setElementHidden(page,"group_scan_submit",true)
        setElementHidden(page,"group_const_submit",true)
        setElementHidden(page,"btn_stepID",false)
      elseif data[1]["Result"] == 0 or data[2]["Result"] == 0 or data[3]["Result"] == 0 then
        if data[1]["Result"] == 0 and data[2]["Result"] == 0 and data[3]["Result"] == 0 then
          page:showToast("三个号码都未通过验证")
        elseif data[1]["Result"] == 0 and data[2]["Result"] == 0 and data[3]["Result"] == 1 then
          page:showToast("号码1和号码2未通过验证")
        elseif data[1]["Result"] == 0 and data[2]["Result"] == 1 and data[3]["Result"] == 0 then
          page:showToast("号码1和号码3未通过验证")
        elseif data[1]["Result"] == 0 and data[2]["Result"] == 1 and data[3]["Result"] == 1 then
          page:showToast("号码1未通过验证")
        elseif data[1]["Result"] == 1 and data[2]["Result"] == 0 and data[3]["Result"] == 0 then
          page:showToast("号码2和号码3未通过验证")
        elseif data[1]["Result"] == 1 and data[2]["Result"] == 0 and data[3]["Result"] == 1 then
          page:showToast("号码2未通过验证")
        elseif data[1]["Result"] == 1 and data[2]["Result"] == 1 and data[3]["Result"] == 0 then
          page:showToast("号码3未通过验证")
        end
      end
    end
    setNumNoticeVisible(page,data[1]["Result"],data[2]["Result"],data[3]["Result"])
  else
    page:showToast(response["resultMsg"])
  end
end

function setNumNoticeVisible(page,type1,type2,type3)
	page:log("setNumNoticeVisible : "..type1.." : "..type2.." : "..type3)
	if type1==0 then
	setElementValue(page,"num_notice1","未通过")
		else 
		setElementValue(page,"num_notice1","通过")
	end
	
	if type2==0 then
	setElementValue(page,"num_notice2","未通过")
		else 
		setElementValue(page,"num_notice2","通过")
	end
	
	if type3==0 then
	setElementValue(page,"num_notice3","未通过")
		else 
		setElementValue(page,"num_notice3","通过")
	end
	
	setElementHidden(page,"num_notice1",false)
    setElementHidden(page,"num_notice2",false)
    setElementHidden(page,"num_notice3",false)
end

function CallLogsExceptionCallBack(page)
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


-----^_^.^_^.^_^.^_^.^_^-------------步骤1-------------^_^.^_^.^_^.^_^.^_^-----

--是否免费
local nextCharge
function onChargeSelected(page,this,type)
  if nextCharge ~= nil and nextCharge ~= this then
    nextCharge:setSelected(false)
  end
  nextCharge=this
  this:setSelected(true)
  
  charge=type
  if charge=="free" then
  setElementHidden(page,"id_sim",false)
  setElementHidden(page,"id_imsi",false)
  end
  
  if charge == "notFree" then
  setElementHidden(page,"id_sim",true)
  setElementHidden(page,"id_imsi",true)
  end
  
  local popupView=page:getElementById("pop_notice")
  popupView:dismiss()
  
end

function showPopupView(page)
 --显示
  local popupView=page:getElementById("pop_notice")
  popupView:showInFragment("center",0,0)
  --显示
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
  local sim=getElementValue(page,"card_simID")
  if sim == "" or sim == nil then
    page:showToast("请输入SIM卡号")
  return
  end
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/backup/queryIMSIInfo.do",
    LuaNetCallBack="IMSINetCallBack(page,{response})",
    LuaExceptionCallBack="IMSIExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",cardId=sim,puk=""},
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

--下一步
function onNextStep(page)
  if charge == "" or charge == nil then
    page:showToast("请选择是否免费")
  return
  end

  local sim=getElementValue(page,"card_simID")
  local imsi=getElementValue(page,"card_imsiID")
  if sim == "" or sim == nil then
    page:showToast("请输入SIM卡号")
  return
  end
  
  if imsi == "" or imsi == nil then
    page:showToast("请输入IMSI号")
  return
  end
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/backup/standbyGet.do",
    LuaNetCallBack="NextStepNetCallBack(page,{response})",
    LuaExceptionCallBack="NextStepExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",simId=sim,stationId="",permitUnactive="1"},
    Method="POST",
    ResponseType="JSON",     
    Message="正在申请"
  }
  page:request(Parameters);   
end

function NextStepNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    setElementValue(page,"pay_nameID",getElementValue(page,"btn_handleID"))
    if charge == "free" then
      setElementValue(page,"pay_actualID","￥0.00")
      setElementHidden(page,"group_payID",true)  
    elseif charge == "notFree" then
      setElementValue(page,"pay_actualID","￥2.00")
      onPaymentSelected(page,posButton,"pos")
      setElementHidden(page,"group_payID",false)
    end
  
    setElementHidden(page,"group_applyID",true)
    setElementHidden(page,"group_apply2ID",false)
    onStartImageResult(page)  
  else
    page:showToast(response["resultMsg"])
  end
end

function NextStepExceptionCallBack(page)
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


-----^_^.^_^.^_^.^_^.^_^-------------步骤2-------------^_^.^_^.^_^.^_^.^_^-----

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
function onContractDetail(page)
  local managers=page:getActivity():decrypt(userInfoSP:getString("managers",""))
  local signatureURL=getElementValue(page,"signature_urlID")
  local sim=getElementValue(page,"card_simID")
  local payName=getElementValue(page,"pay_nameID")
  local bundle=page:initBundle()
    bundle:putString("protocol","replacementCard")
    bundle:putString("signatureURL",signatureURL)
    if payName == "补卡" then
      bundle:putString("businessType","0")
    elseif payName == "换卡" then
      bundle:putString("businessType","1")
    end
    if charge == "free" then
      bundle:putString("actual","0.00")    
    elseif charge == "notFree" then
      bundle:putString("actual","2.00")    
    end    
    bundle:putString("discount","0.00")
    bundle:putString("sim",sim)
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
    setElementValue(page,"write_signatureID","local://"..signaturePath)
    setElementValue(page,"signature_urlID",signaturePath)
  end
end

--拍照
function onPhotograph(page,this)
  local isPhotograph=this:getElementById("isPhotographID"):getValue()
  if isPhotograph == "photograph" then
    local payName=getElementValue(page,"pay_nameID")
    if payName == "补卡" then
      page:getActivity():photograph(mobileNum,"BackupCard")
    elseif payName == "换卡" then
      page:getActivity():photograph(mobileNum,"ExchangeCard")
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
function onSubmit(page)
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
    RequestParams={phoneNum=mobileNum,businessType="补换卡",remark=remark},
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

--办理业务
function onBusinessHandle(page)
  page:getActivity():showProgreesDialog("正在办理")
  local Parameters={
    NetSelector="NetData:",                
    URL="/backup/cardActivation.do",
    LuaNetCallBack="BusinessHandleNetCallBack(page,{response})",
    LuaExceptionCallBack="BusinessHandleExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",cardType="4",permitUnactive="1"},
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
    setElementValue(page,"pass_wordID","您已成功办理补换卡！\nIMSI号码为："..getElementValue(page,"card_imsiID"))
    setElementHidden(page,"group_apply2ID",true)
    setElementHidden(page,"group_passID",false)
    --倒计时
    onCountDown(page,"btn_replacementCardID")
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

--重新进行停开机
function onAgain(page)
  setElementEnable(page,"btn_submitID",false)
  setElementHidden(page,"group_apply2ID",false)
  setElementHidden(page,"group_failID",true)
end

function goBack(page)
  page:setCustomAnimations("right")
  page:goBack()  
end

function onBackPressed(page)
  goBack(page)
end