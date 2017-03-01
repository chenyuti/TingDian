
function PageInit(page)
  page:postDelayed("onNextPage(page)",3000)
end

function onNextPage(page)
  page:setCustomAnimations("fade")
  page:nextPage("res://loginPage.xml",false,nil)
end