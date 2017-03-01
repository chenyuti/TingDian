require ("public")
local userInfoSP
local maxView
local aveMaxView
local aveMinView
local minView
local trend1View,trend2View,trend3View,trend4View,trend5View,trend6View

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  maxView=page:getElementById("maxID")
  aveMaxView=page:getElementById("ave_maxID")
  aveMinView=page:getElementById("ave_minID")
  minView=page:getElementById("minID")
 
  trend1View=page:getElementById("trend1ID")
  trend2View=page:getElementById("trend2ID")
  trend3View=page:getElementById("trend3ID")
  trend4View=page:getElementById("trend4ID")
  trend5View=page:getElementById("trend5ID")
  trend6View=page:getElementById("trend6ID")

  local month1=page:getArguments():getString("month1")
  local month2=page:getArguments():getString("month2")
  local month3=page:getArguments():getString("month3")
  local month4=page:getArguments():getString("month4")
  local month5=page:getArguments():getString("month5")
  local month6=page:getArguments():getString("month6")
  local trend1=page:getArguments():getString("month1Sum")
  local trend2=page:getArguments():getString("month2Sum")
  local trend3=page:getArguments():getString("month3Sum")
  local trend4=page:getArguments():getString("month4Sum")
  local trend5=page:getArguments():getString("month5Sum")
  local trend6=page:getArguments():getString("month6Sum")
  local monthNum=page:getArguments():getString("monthNum")
 
  setElementValue(page,"month1ID",month1)
  setElementValue(page,"month2ID",month2)
  setElementValue(page,"month3ID",month3)
  setElementValue(page,"month4ID",month4)
  setElementValue(page,"month5ID",month5)
  setElementValue(page,"month6ID",month6)
  
  local avg=(trend1+trend2+trend3+trend4+trend5+trend6)/6
  local max=string.format("%d",avg*2)
 
  local maxValue=tonumber(trend1)
  if maxValue < tonumber(trend2) then
    maxValue=tonumber(trend2)
  end
  if maxValue < tonumber(trend3) then
    maxValue=tonumber(trend3)
  end
  if maxValue < tonumber(trend4) then
    maxValue=tonumber(trend4)
  end
  if maxValue < tonumber(trend5) then
    maxValue=tonumber(trend5)
  end
  if maxValue < tonumber(trend6) then
    maxValue=tonumber(trend6)
  end
 
  if maxValue > tonumber(max) then
    max=string.format("%d",maxValue)
  end
 
  local min=string.format("%d",max/4)
  local avg=string.format("%d",max/2)
  local avgMax=string.format("%d",max*3/4)
  maxView:setValue(""..max)
  minView:setValue(""..min)
  aveMinView:setValue(""..avg)
  aveMaxView:setValue(""..avgMax)
 
  trend1View:setMaxProgress(""..max)
  trend2View:setMaxProgress(""..max)
  trend3View:setMaxProgress(""..max)
  trend4View:setMaxProgress(""..max)
  trend5View:setMaxProgress(""..max)
  trend6View:setMaxProgress(""..max)
 
  if monthNum == "1" then
    trend1View:setColor("#e40177")
    trend1View:setTextColor("#e40177")
  elseif monthNum == "2" then
    trend2View:setColor("#e40177")
    trend2View:setTextColor("#e40177")
  elseif monthNum == "3" then
    trend3View:setColor("#e40177")
    trend3View:setTextColor("#e40177")
  elseif monthNum == "4" then
    trend4View:setColor("#e40177")
    trend4View:setTextColor("#e40177")
  elseif monthNum == "5" then
    trend5View:setColor("#e40177")
    trend5View:setTextColor("#e40177")
  elseif monthNum == "6" then
    trend6View:setColor("#e40177")
    trend6View:setTextColor("#e40177")
  end

  trend1View:setCurrProgress(trend1)
  trend2View:setCurrProgress(trend2)
  trend3View:setCurrProgress(trend3)
  trend4View:setCurrProgress(trend4)
  trend5View:setCurrProgress(trend5)
  trend6View:setCurrProgress(trend6)
end

function goBack(page)
  page:setCustomAnimations("right")
  page:goBack()
end

function onBackPressed(page)
  goBack(page)
end