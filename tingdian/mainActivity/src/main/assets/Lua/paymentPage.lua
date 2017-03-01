require ("public")
local userInfoSP
local imagePopupView
local posButton
local mobileNum --客户电话号码
local owing
local picturePath=""

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  imagePopupView=page:getElementById("image_popupID")
  posButton=page:getElementById("btn_posID")
  
  mobileNum=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
  if mobileNum == "" then
    setElementHidden(page,"group_loginID",true)    
    setElementHidden(page,"group_notLoginID",false)
    setElementEnable(page,"next_stepID",true)    
  else
    setElementHidden(page,"group_loginID",false)    
    setElementHidden(page,"group_notLoginID",true)    
    customerDetail(page)
  end  
end


-----^_^.^_^.^_^.^_^.^_^-------------小左边-------------^_^.^_^.^_^.^_^.^_^-----

function onLogin(page)
  mobileNum=getElementValue(page,"mobileID")
  if mobileNum == "" then
    page:showToast("请输入手机号")
  return
  end
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/personalMsg.do",
    LuaNetCallBack="LoginNetCallBack(page,{response})",
    LuaExceptionCallBack="LoginExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel=""},
    Method="POST",
    ResponseType="JSON",     
    Message="正在加载"
  }
  page:request(Parameters);
end

function LoginNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    setElementValue(page,"customer_mobileID",mobileNum)
    setElementValue(page,"customer_brandID",data["Brand"])
    
    if data["RealInfoCode"] == "000" or data["RealInfoCode"] == "1" then
      setElementValue(page,"customer_realNameID","已实名登记")
    else
      setElementValue(page,"customer_realNameID","未实名登记")
    end    
    
    if data["Status"] == "0" then
      setElementValue(page,"customer_stopOnID","已停机")
    elseif data["Status"] == "1" then
      setElementValue(page,"customer_stopOnID","正使用")
    end    
    
    setElementValue(page,"customer_balanceID","￥"..data["UsableFee"])
    if string.find(data["UsableFee"],"-") ~= nil then
      local shouldPay=string.sub(data["UsableFee"],2,string.len(data["UsableFee"]))
      setElementValue(page,"should_payID","￥"..shouldPay)
      owing=true
    else
      setElementValue(page,"should_payID","￥0")
      owing=false
    end  
    
    onPaymentSelected(page,posButton,"pos")
    
    setElementHidden(page,"group_loginID",false)    
    setElementHidden(page,"group_notLoginID",true)
    setElementEnable(page,"next_stepID",false)    
    
    local edit=userInfoSP:edit()
      edit:putString("managers",page:getActivity():encrypt("false"))
      edit:commit()        
  else
    page:showToast(response["resultMsg"])
  end
end

function LoginExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function customerDetail(page)
  setElementValue(page,"customer_mobileID",mobileNum)
  local customerBrand=page:getActivity():decrypt(userInfoSP:getString("brand",""))
  local customerRealName=page:getActivity():decrypt(userInfoSP:getString("realName",""))
  local customerStopOn=page:getActivity():decrypt(userInfoSP:getString("stopOn",""))
  local customerBalance=page:getActivity():decrypt(userInfoSP:getString("balance",""))

  setElementValue(page,"customer_brandID",customerBrand)
  
  if customerRealName == "000" or customerRealName == "1" then
    setElementValue(page,"customer_realNameID","已实名登记")
  else
    setElementValue(page,"customer_realNameID","未实名登记")
  end    
  
  if customerStopOn == "0" then
    setElementValue(page,"customer_stopOnID","已停机")
  elseif customerStopOn == "1" then
    setElementValue(page,"customer_stopOnID","正使用")
  end    
  
  setElementValue(page,"customer_balanceID","￥"..customerBalance)
  if string.find(customerBalance,"-") ~= nil then
    local shouldPay=string.sub(customerBalance,2,string.len(customerBalance))
    setElementValue(page,"should_payID","￥"..shouldPay)
    owing=true
  else
    setElementValue(page,"should_payID","￥0")
    owing=false
  end  
  
  setElementEnable(page,"next_stepID",false)    
  onPaymentSelected(page,posButton,"pos")
end


-----^_^.^_^.^_^.^_^.^_^-------------缴费步骤1-------------^_^.^_^.^_^.^_^.^_^-----

--选择支付金额档次
local nextPrice
function onPrice(page,this,value)
  if this:isSelected() then
    this:setSelected(false)
    setElementValue(page,"pay_costID","")
  return
  end

  if nextPrice ~= nil and nextPrice ~= this then
    nextPrice:setSelected(false)
  end
  nextPrice=this
  this:setSelected(true)
  setElementValue(page,"pay_costID",value)
end

--选择POS机or现金
local nextPay
local paySelected --支付方式选择，POS机=pos，现金=cash
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
  paySelected=type
end

--下一步
function onNextStep(page)
  local payCoast=getElementValue(page,"pay_costID")
  if payCoast == "" or payCoast == nil then
    page:showToast("请输入金额")
  return
  end
  
  local payNum=getElementValue(page,"pos_numID")
  if paySelected == "pos" and payNum == "" then
    page:showToast("请输入POS单号")
  return
  end
  
  setElementValue(page,"pay_castID","￥"..payCoast)
  if paySelected == "pos" then
    setElementValue(page,"pay_typeID","POS机".."（单号:"..payNum.."）")
  elseif paySelected == "cash" then
    setElementValue(page,"pay_typeID","现金")
  end
  onStartImageResult(page)
    
  setElementHidden(page,"group_applyID",true)
  setElementHidden(page,"group_apply2ID",false)
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


-----^_^.^_^.^_^.^_^.^_^-------------缴费步骤2-------------^_^.^_^.^_^.^_^.^_^-----

--返回上一步
function onLastBack(page)
  setElementHidden(page,"group_applyID",false)
  setElementHidden(page,"group_apply2ID",true)
end

--业务单详情
function onProtocol(page)
  local managers=page:getActivity():decrypt(userInfoSP:getString("managers",""))
  local signatureURL=getElementValue(page,"signature_urlID")
  local payCoast=getElementValue(page,"pay_costID")
  local bundle=page:initBundle()
    bundle:putString("protocol","payment")
    bundle:putString("signatureURL",signatureURL)
    bundle:putString("actual",payCoast)
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
  local isPhotograph=this:getElementById("picture_typeID"):getValue()
  if isPhotograph == "photograph" then
    page:getActivity():photograph(mobileNum,"PayCharge")
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
    RequestParams={phoneNum=mobileNum,businessType="缴费",remark=remark},
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
  page:showToast("网络异常！")
  setElementEnable(page,"btn_submitID",false)
  page:hideProgreesDialog()
end

--办理业务
local brandType
local posNum
local payType
function onBusinessHandle(page)
  page:getActivity():showProgreesDialog("正在办理")
  local customerBrand=getElementValue(page,"customer_brandID")
  local payCoast=getElementValue(page,"pay_costID")
  if customerBrand == "全球通" then
    brandType="1"
  else
    brandType="2"
  end
  
  if paySelected == "pos" then  
    payType="5"
    posNum=getElementValue(page,"pos_numID")
  elseif paySelected == "cash" then
    payType="1"
    posNum=""
  end
    
  local Parameters={
    NetSelector="NetData:",                
    URL="/recharge/rechargeTradeBankPayTrans.do",
    LuaNetCallBack="BusinessHandleNetCallBack(page,{response})",
    LuaExceptionCallBack="BusinessHandleExceptionCallBack(page)",
    RequestParams={channel="",phoneNum=mobileNum,sequence=posNum,type=brandType,amount=""..payCoast*100,organizationId="00",paymMode="00",payOrg="00",payType=payType,fee=""..payCoast*100,paySeq=posNum},
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
    setElementHidden(page,"group_apply2ID",true)
    setElementHidden(page,"group_passID",false)
    
    local payCoast=getElementValue(page,"pay_costID")
    local owingMoney=string.sub(getElementValue(page,"should_payID"),4,string.len(getElementValue(page,"should_payID")))
    if owing == true then
      if tonumber(payCoast) >= tonumber(owingMoney) then
        setElementValue(page,"customer_balanceID","￥"..payCoast-owingMoney)
        setElementValue(page,"should_payID","￥0")
        local edit=userInfoSP:edit()
        edit:putString("balance",page:getActivity():encrypt(""..payCoast-owingMoney))
        edit:commit()
        owing=false    
      else
        setElementValue(page,"customer_balanceID","￥-"..owingMoney-payCoast)
        setElementValue(page,"should_payID","￥"..owingMoney-payCoast)
        local edit=userInfoSP:edit()
        edit:putString("balance",page:getActivity():encrypt("-"..owingMoney-payCoast))
        edit:commit()    
      end
    elseif owing == false then
      local customerBalance=page:getActivity():decrypt(userInfoSP:getString("balance",""))
      setElementValue(page,"customer_balanceID","￥"..customerBalance+payCoast)
      setElementValue(page,"should_payID","￥0")
      local edit=userInfoSP:edit()
      edit:putString("balance",page:getActivity():encrypt(""..customerBalance+payCoast))
      edit:commit()    
    end  
    --倒计时
    onCountDown(page,"btn_paymentID")  
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
--  page:hideProgreesDialog()
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