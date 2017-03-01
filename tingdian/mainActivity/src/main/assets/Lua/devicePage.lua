require ("public")
local userInfoSP

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  
  local bluetoothDevice=page:getActivity():decrypt(userInfoSP:getString("bluetoothDevice",""))
  setElementValue(page,"device_defaultID",bluetoothDevice)
  
  page:getActivity():BluetoothDevice()
  page:getActivity():sleep(page,nil,"onStopSearch(page)",10000)
end

function onStopSearch(page)
  page:getActivity():stopBluetoothSearch()
end

function DeviceResult(page)
  page:getActivity():hideProgreesDialog()
  local deviceArray=page:getActivity():getDeviceList()
  local device=split(deviceArray,",")
  local deviceSize=table.getn(device)
  
  local listView=page:getElementById("device_listID")
  listView:getEntity():getTemplateData():clear()
  listView:getEntity():setTemplateDataStyle("device")
  local entityData={}
  for i=1,deviceSize-1,1 do
    entityData[i]={subEntityValues={device_nameID=device[i]}}
  end
  listView:upDateEntity(entityData)
end

--选择默认连接设备
local nextDevice
local deviceName
function onDeviceSelected(page,this)
  deviceName=this:getElementById("device_nameID"):getValue()
  if nextDevice ~= nil and nextDevice ~= this then
    nextDevice:setSelected(false)
  end
  this:setSelected(true)
  nextDevice=this
end

--确认
function onSure(page)
  page:getActivity():showProgreesDialog("正在连接证件扫描仪")
  page:getActivity():sleep(page,nil,"onBluetoothScanning(page)",500)
end

function onBluetoothScanning(page)
  page:getActivity():connectScanning(deviceName)
  local deviceConnect=page:getActivity():decrypt(userInfoSP:getString("deviceConnect",""))
  if deviceConnect == "true" then
    goBack(page)
  end
end

function goBack(page)
  page:setCustomAnimations("right")
  page:goBack()  
end

function onBackPressed(page)
  goBack(page)
end