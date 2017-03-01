require ("public")
local userInfoSP
local imagePopupView
local mobileNum --客户电话号码
local picturePath=""

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
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

--获取账户信息
function customerDetail(page)
  mobileNum=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
  local customerBrand=page:getActivity():decrypt(userInfoSP:getString("brand",""))
  local customerRealName=page:getActivity():decrypt(userInfoSP:getString("realName",""))
  local customerCardType=page:getActivity():decrypt(userInfoSP:getString("cardType",""))
  local customerUserName=page:getActivity():decrypt(userInfoSP:getString("userName",""))
  local customerUserNum=page:getActivity():decrypt(userInfoSP:getString("userNum",""))
  local customerUserAddress=page:getActivity():decrypt(userInfoSP:getString("userAddress",""))    
  
  setElementValue(page,"customer_mobileID",mobileNum)
  setElementValue(page,"customer_brandID",customerBrand)
  
  if customerRealName == "1" then
    setElementValue(page,"customer_realNameID","已实名登记")
  end
  
  if customerCardType == "" and customerUserName == "" and customerUserNum == "" and customerUserAddress == "" then
    setElementHidden(page,"group_infoID",true)
  end
  
  --个人
  if customerCardType == "IdCard" or customerCardType == "TaiBaoZheng" or customerCardType == "SoldierID" or customerCardType == "PolicePaper" or customerCardType == "Passport" or customerCardType == "HuKouBu" or customerCardType == "HKIdCard" then
    setElementValue(page,"customer_cardTypeID","个人")
  --单位
  elseif customerCardType == "CorpLicence" or customerCardType == "BusinessLicence" or customerCardType == "UnitID" or customerCardType == "OrgaLicence" or customerCardType == "OrgCodeCard" then
    setElementValue(page,"customer_cardTypeID","单位")
  else
    setElementHidden(page,"customer_cardTypeID",true)
  end
  if customerUserName ~= "" then
    setElementValue(page,"customer_nameID",string.sub(customerUserName,0,3).."＊＊")
  else
    setElementHidden(page,"customer_nameID",true)
  end
  if customerUserNum ~= "" then
    setElementValue(page,"customer_numID",string.sub(customerUserNum,0,6).."******"..string.sub(customerUserNum,13,string.len(customerUserNum)))
  else
    setElementHidden(page,"customer_numID",true)
  end  
  if customerUserAddress ~= "" then
    setElementValue(page,"customer_addressID",string.sub(customerUserAddress,0,7).."******"..string.sub(customerUserAddress,12,string.len(customerUserAddress)))  
  else
    setElementHidden(page,"customer_addressID",true)
  end  
end


-----^_^.^_^.^_^.^_^.^_^-------------步骤-------------^_^.^_^.^_^.^_^.^_^-----

--业务单详情
function onProtocol(page)
  local signatureURL=getElementValue(page,"signature_urlID")
  local bundle=page:initBundle()
    bundle:putString("protocol","removeRealName")
    bundle:putString("signatureURL",signatureURL)
    page:setCustomAnimations("left")
    page:nextPage("res://protocolPage.xml",false,bundle) 
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
    page:getActivity():photograph(mobileNum,"UnsubscribeRealName")
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
    RequestParams={phoneNum=mobileNum,businessType="退登记",remark=remark},
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
  page:getActivity():showProgreesDialog("正在办理")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/regstatusmodify.do",
    LuaNetCallBack="BusinessHandleNetCallBack(page,{response})",
    LuaExceptionCallBack="BusinessHandleExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum},
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
    local edit=userInfoSP:edit()
      edit:putString("realName","")
      edit:putString("userName","")
      edit:putString("userNum","")
      edit:putString("userAddress","")
      edit:commit()   
      --倒计时
    onCountDown(page,"btn_removeRealNameID") 
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