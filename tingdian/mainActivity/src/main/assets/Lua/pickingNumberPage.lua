require ("public")
local userInfoSP
local num1View
local num2View
local num3View
local num4View
local num5View
local num6View
local num7View
local num8View
local num9View
local num10View
local num11View

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  num1View=page:getElementById("num1")
  num2View=page:getElementById("num2")
  num3View=page:getElementById("num3")
  num4View=page:getElementById("num4")
  num5View=page:getElementById("num5")
  num6View=page:getElementById("num6")
  num7View=page:getElementById("num7")
  num8View=page:getElementById("num8")
  num9View=page:getElementById("num9")
  num10View=page:getElementById("num10")
  num11View=page:getElementById("num11")
  
  onMobileList(page)
end

function onMobileList(page)
  local Parameters={
    NetSelector="NetData:",                
    URL="/account/queryReserveNumInfo.do",
    LuaNetCallBack="MobileNetCallBack(page,{response})",
    LuaExceptionCallBack="MobileExceptionCallBack(page)",
    RequestParams={phoneNum="",channel="",IP="",mainProdId="0",pattern="",maxPrice="",minPrice="",isPrice="0"},
    Method="POST",
    ResponseType="JSON",     
    Message="正在加载"
  }
  page:request(Parameters);
end  

function MobileNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    setElementValue(page,"selected_numID","")
    if #data == 0 then
      setElementHidden(page,"group_notListID",false)
      setElementHidden(page,"mobile_listID",true)      
    else
      setElementHidden(page,"group_notListID",true)
      setElementHidden(page,"mobile_listID",false)
      local listView=page:getElementById("mobile_listID")
      listView:getEntity():getTemplateData():clear()
      local entityData={}
      for i=1,#data,1 do
        local itemData=data[i]
        entityData[i]={subEntityValues={mobileID=""..itemData["TelNum"],price_ID=itemData["Price"],priceID="预存￥"..(itemData["Price"]/100)},attributes={selected="false"}}
      end    
      listView:upDateEntity(entityData)    
    end
  else
    page:showToast(response["resultMsg"])
  end
end

function MobileExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

local checkNum=""
function onTextChanged(page,this,id)
  local num1=num1View:getValue()
  local num2=num2View:getValue()
  local num3=num3View:getValue()
  local num4=num4View:getValue()
  local num5=num5View:getValue()
  local num6=num6View:getValue()
  local num7=num7View:getValue()
  local num8=num8View:getValue()
  local num9=num9View:getValue()
  local num10=num10View:getValue()
  local num11=num11View:getValue()
  
  if id == "1" and num1 ~= "" then
    num2View:showKeyboard()
  elseif id == "2" and num2 ~= "" then
    num3View:showKeyboard()
  elseif id == "3" and num3 ~= "" then
    num4View:showKeyboard()
  elseif id == "4" and num4 ~= "" then
    num5View:showKeyboard()
  elseif id == "5" and num5 ~= "" then
    num6View:showKeyboard()
  elseif id == "6" and num6 ~= "" then
    num7View:showKeyboard()
  elseif id == "7" and num7 ~= "" then
    num8View:showKeyboard()
  elseif id == "8" and num8 ~= "" then
    num9View:showKeyboard()
  elseif id == "9" and num9 ~= "" then
    num10View:showKeyboard()
  elseif id == "10" and num10 ~= "" then
    num11View:showKeyboard()
  end

  if num1 == "" and num2 == "" and num3 == "" and num4 == "" and num5 == "" and num6 == "" and num7 == "" and num8 == "" and num9 == "" and num10 == "" and num11 == "" then
    checkNum=""
  else  
    if num1 == "" then
      num1="%"
    end
    if num2 == "" then
      num2="%"
    end
    if num3 == "" then
      num3="%"
    end
    if num4 == "" then
      num4="%"
    end
    if num5 == "" then
      num5="%"
    end
    if num6 == "" then
      num6="%"
    end
    if num7 == "" then
      num7="%"
    end
    if num8 == "" then
      num8="%"
    end
    if num9 == "" then
      num9="%"
    end
    if num10 == "" then
      num10="%"
    end
    if num11 == "" then
      num11="%"
    end
    checkNum=num1..num2..num3..num4..num5..num6..num7..num8..num9..num10..num11
  end
end  

--查询号码
function onCheck(page)
  local minPrice=getElementValue(page,"min_priceID")
  local maxPrice=getElementValue(page,"max_priceID")
  local pattern=checkNum
  pattern=string.gsub(pattern,"%%%%%%%%%%%%%%%%%%%%","%%")
  pattern=string.gsub(pattern,"%%%%%%%%%%%%%%%%%%","%%")
  pattern=string.gsub(pattern,"%%%%%%%%%%%%%%%%","%%")
  pattern=string.gsub(pattern,"%%%%%%%%%%%%%%","%%")
  pattern=string.gsub(pattern,"%%%%%%%%%%%%","%%")
  pattern=string.gsub(pattern,"%%%%%%%%%%","%%")
  pattern=string.gsub(pattern,"%%%%%%%%","%%")
  pattern=string.gsub(pattern,"%%%%%%","%%")
  pattern=string.gsub(pattern,"%%%%","%%")
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/account/queryReserveNumInfo.do",
    LuaNetCallBack="MobileNetCallBack(page,{response})",
    LuaExceptionCallBack="MobileExceptionCallBack(page)",
    RequestParams={phoneNum="",channel="",IP="",mainProdId="0",pattern=pattern,maxPrice=maxPrice,minPrice=minPrice,isPrice=""},
    Method="POST",
    ResponseType="JSON",     
    Message="正在加载"
  }
  page:request(Parameters);
end

--自由选号
function onFreedom(page)
  onMobileList(page)
end

--选择号码
local nextNumber
function onSelectedNumber(page,this)
  if nextNumber ~= nil and nextNumber ~= this then
    nextNumber:setSelected(false)
  end
  nextNumber=this
  this:setSelected(true)
    
  local selectedNumber=this:getElementById("mobileID"):getValue()
  local selectedPrice=this:getElementById("price_ID"):getValue()
  setElementValue(page,"selected_numID",selectedNumber)
  setElementValue(page,"selected_priceID",selectedPrice)
end

--确认号码
function onSureNumber(page)
  local selectedNumber=getElementValue(page,"selected_numID")
  local selectedPrice=getElementValue(page,"selected_priceID")
  if selectedNumber == "" or selectedNumber == nil then
    page:showToast("请先选择电话号码")
  return
  end
  
  local bundle=page:initBundle()
  bundle:putString("selectedMobile",selectedNumber)
  bundle:putString("selectedPrice",selectedPrice)
  page:setCustomAnimations("right")
  page:goBack(bundle)  
end

function goBack(page)
  page:setCustomAnimations("right")
  page:goBack()  
end

function onBackPressed(page)
  goBack(page)
end