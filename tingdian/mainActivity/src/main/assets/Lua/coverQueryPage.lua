require ("public")
local userInfoSP

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
end

local areaWord="福田区"
function onValueChange(page,this)
  local areaValue=getElementValue(page,"areaID")
  local searchValue=getElementValue(page,"searchID")
  if areaValue ~= areaWord and searchValue ~= "" then
    areaWord=areaValue
    onAddressList(page,areaValue,searchValue)
  end
end

local searchWord=""
function onTextChanged(page,this)
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
  local entrance=page:getArguments():getString("entrance")
  if entrance == "broadband" then
    local bundle=page:initBundle()
      bundle:putString("newAddress",selectedAddress)  
      page:setCustomAnimations("right")
      page:goBack(bundle)
  elseif entrance == "service" then
    local bundle=page:initBundle()
      bundle:putString("dealEntrance","cover")  
      bundle:putString("newAddress",selectedAddress)  
      page:setCustomAnimations("left")
      page:nextPage("res://broadbandPage.xml",true,bundle)
  end
end

function goBack(page)
  page:setCustomAnimations("right")
  page:goBack()  
end

function onBackPressed(page)
  goBack(page)
end