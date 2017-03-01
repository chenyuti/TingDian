--设置值
function setElementValue(page,id,value)
  local tempView=page:getElementById(id)
  if tempView then
    tempView:setValue(value)
  end
end

--获取值
function getElementValue(page,id)
  local tempView=page:getElementById(id)
  if tempView then
    return tempView:getValue()
  else
    return ""
  end
end

--是否选择
function setElementSelected(page,id,isSelected)
  local tempView=page:getElementById(id)
  if tempView then
    tempView:setSelected(isSelected)
  end
end

--是否显示隐藏
function setElementHidden(page,id,isHidden)
  local tempView=page:getElementById(id)
  if tempView then
    tempView:setHidden(isHidden)
  end
end

--是否可操作
function setElementEnable(page,id,isEnable)
  local tempView=page:getElementById(id)
  if tempView then
    if isEnable == true then
      tempView:setDisable(true)
    else 
      tempView:setDisable(false)
    end
  end
end

--通用图片上传，获取地址
function getImagePostFilePaths(page,signaturePath,path,size)
  local postFilePaths={}
  if size == 1 then
    postFilePaths = {{"pics1",signaturePath}}
  elseif size == 2 then
    postFilePaths = {{"pics1",signaturePath},{"pics2",path[1]}}
  elseif size == 3 then
    postFilePaths = {{"pics1",signaturePath},{"pics2",path[1]},{"pics3",path[2]}}
  elseif size == 4 then
    postFilePaths = {{"pics1",signaturePath},{"pics2",path[1]},{"pics3",path[2]},{"pics4",path[3]}}
  elseif size == 5 then
    postFilePaths = {{"pics1",signaturePath},{"pics2",path[1]},{"pics3",path[2]},{"pics4",path[3]},{"pics5",path[4]}}
  elseif size == 6 then
    postFilePaths = {{"pics1",signaturePath},{"pics2",path[1]},{"pics3",path[2]},{"pics4",path[3]},{"pics5",path[4]},{"pics6",path[5]}}
  end
  return postFilePaths
end

--客户注销
function onCustomerLoginQuit(page)
  local Parameters={
    NetSelector="NetData:",                
    URL="/login/userValidate.do",
    LuaNetCallBack="CustomerLoginQuitNetCallBack(page,{response})",
    LuaExceptionCallBack="CustomerLoginQuitExceptionCallBack(page)",
    RequestParams={},
    Method="POST",
    ResponseType="JSON",     
    Message="正在注销"
  }
  page:request(Parameters);
end

function CustomerLoginQuitNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local userInfoSP=page:getSharedPreferences("NEWUSERINDO")
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
    edit:commit()
    
    page.manager:removeFragment("res://customerServicePage.xml")    
    page:showToast("注销成功")
    page:setCustomAnimations("right")
    page:goBack()
  else
    page:showToast(response["resultMsg"])
  end
end

function CustomerLoginQuitExceptionCallBack(page)
  page:hideProgreesDialog()
end

function split(str, split_char)
  local sub_str_tab = {};
  while (true) do
    local pos = string.find(str, split_char);
    if (not pos) then
      sub_str_tab[#sub_str_tab + 1] = str;
      break;
    end
    local sub_str = string.sub(str, 1, pos - 1);
    sub_str_tab[#sub_str_tab + 1] = sub_str;
    str = string.sub(str, pos + 1, #str);
  end
  return sub_str_tab;
end

function LuaSplit(str,split)  
  local lcSubStrTab = {}  
  while true do  
    local lcPos = string.find(str,split)  
    if not lcPos then  
      lcSubStrTab[#lcSubStrTab+1] =  str      
      break  
    end  
    local lcSubStr  = string.sub(str,1,lcPos-1)  
    lcSubStrTab[#lcSubStrTab+1] = lcSubStr  
    str = string.sub(str,lcPos+1,#str)  
  end  
  return lcSubStrTab  
end

function LuaRemove(str,remove)  
  local lcSubStrTab = {}  
  while true do  
    local lcPos = string.find(str,remove)  
    if not lcPos then  
      lcSubStrTab[#lcSubStrTab+1] =  str      
      break  
    end  
    local lcSubStr  = string.sub(str,1,lcPos-1)  
    lcSubStrTab[#lcSubStrTab+1] = lcSubStr  
    str = string.sub(str,lcPos+1,#str)  
  end  
  local lcMergeStr =""  
  local lci = 1  
  while true do  
    if lcSubStrTab[lci] then  
      lcMergeStr = lcMergeStr .. lcSubStrTab[lci]   
      lci = lci + 1  
    else   
      break  
    end  
  end  
  return lcMergeStr  
end

local nextCountDown = 5
local ViewId
local flag = true
--倒计时
function onCountDown(page,this)
  if flag then
    ViewId = this
  end
  page:log("执行计时任务..."..ViewId)
  if nextCountDown ~= 0 then
    flag = false
    setElementValue(page,ViewId,"点击立即返回,"..nextCountDown.."秒后自动返回客户服务")
    nextCountDown=nextCountDown-1
    page:postDelayed("onCountDown(page,this)",1000)
  else
    flag = false
    nextCountDown=0
    backServicePage(page)
  end
end

function backServicePage(page)
    page:goBack() 
--业务办理完成,跳转到客户服务界面
    page:nextPage("res://customerServicePage.xml",false,nil)

end

--  业务下架
function onBusinessPause(page)
  page:showToast("该业务已下架")
end


