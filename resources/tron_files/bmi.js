var bmi_htmlEdit=0;var bmi_ie;var bmi_ns;var bmi_safari;var bmi_imageObjSelected;var bmi_ffx_op_toolTip="Shift+R improves the quality of this image. Shift+A improves the quality of all images on this page.";var bmi_toolTip="Shift+R improves the quality of this image. CTRL+F5 reloads the whole page.";var bmi_ns_tooltip="Shift+Reload reloads the whole page.";var bmi_toolTipSeperator=" ... ";var bmi_concatStr="bmi_orig_img";var bmi_frameNotAllowed;var agt=navigator.userAgent.toLowerCase();var is_major=parseInt(navigator.appVersion);var is_minor=parseFloat(navigator.appVersion);var bmi_ns=((agt.indexOf('mozilla')!=-1)&&(agt.indexOf('spoofer')==-1)&&(agt.indexOf('compatible')==-1)&&(agt.indexOf('opera')==-1)&&(agt.indexOf('webtv')==-1)&&(agt.indexOf('hotjava')==-1));var bmi_ns2=(bmi_ns&&(is_major==2));var bmi_ns3=(bmi_ns&&(is_major==3));var bmi_ns4=(bmi_ns&&(is_major==4));var bmi_ns4up=(bmi_ns&&(is_major>=4));var bmi_nsonly=(bmi_ns&&((agt.indexOf(";nav")!=-1)||(agt.indexOf("; nav")!=-1)||(agt.indexOf("Netscape")!=-1)||(agt.indexOf("netscape")!=-1)));var bmi_ns6=(bmi_ns&&(is_major==5));var bmi_ns6up=(bmi_ns&&(is_major>=5));var is_gecko=(agt.indexOf('gecko')!=-1);var bmi_firefox=(agt.indexOf('firefox')!=-1);var bmi_safari=(agt.indexOf('applewebkit')!=-1);var bmi_ie=((agt.indexOf("msie")!=-1)&&(agt.indexOf("opera")==-1));var bmi_ie3=(bmi_ie&&(is_major<4));var bmi_ie4=(bmi_ie&&(is_major==4)&&(agt.indexOf("msie 4")!=-1));var bmi_ie4up=(bmi_ie&&(is_major>=4));var bmi_ie5=(bmi_ie&&(is_major==4)&&(agt.indexOf("msie 5.0")!=-1));var bmi_ie5_5=(bmi_ie&&(is_major==4)&&(agt.indexOf("msie 5.5")!=-1));var bmi_ie5up=(bmi_ie&&!bmi_ie3&&!bmi_ie4);var bmi_ie5_5up=(bmi_ie&&!bmi_ie3&&!bmi_ie4&&!bmi_ie5);var bmi_ie6=(bmi_ie&&(is_major==4)&&(agt.indexOf("msie 6.")!=-1));var bmi_ie6up=(bmi_ie&&!bmi_ie3&&!bmi_ie4&&!bmi_ie5&&!bmi_ie5_5);var bmi_opera=(agt.indexOf("opera")!=-1);var bmi_opera2=(agt.indexOf("opera 2")!=-1||agt.indexOf("opera/2")!=-1);var bmi_opera3=(agt.indexOf("opera 3")!=-1||agt.indexOf("opera/3")!=-1);var bmi_opera4=(agt.indexOf("opera 4")!=-1||agt.indexOf("opera/4")!=-1);var bmi_opera5=(agt.indexOf("opera 5")!=-1||agt.indexOf("opera/5")!=-1);var bmi_opera5up=(bmi_opera&&!bmi_opera2&&!bmi_opera3&&!bmi_opera4);function bmi_checkAccess(win){bmi_frameNotAllowed=0;window.bmioldOnError=window.onerror;window.onerror=null;try{var l=win.location.href;}
catch(e){bmi_frameNotAllowed=1;}
if(bmi_frameNotAllowed==1){window.onerror=window.bmioldOnError;return false;}
else{window.onerror=window.bmioldOnError;return true;}}
function bmi_ImageElement(el){if(!el)
return 0;var str=new String(el.tagName);if(str.match("IMG")){return 1;}
if(str.match("INPUT")){if(el.type&&bmi_checkInputType(el.type)){return 1;}
return 0;}
if(str.match("OBJECT")){if(el.type&&bmi_checkMIMEType(el.type)){el.bmi_objTag=1;return 1;}}
if(str.match("EMBED")){if(el.type&&bmi_checkMIMEType(el.type)){return 1;}}
if(str.match("AREA")||str.match("A")){var p=el.parentNode;if(p&&(p.tagName=="MAP")&&(p.bmi_imgObj!=null)){el.bmi_mapImage=p.bmi_imgObj;p.bmi_imgObj.bmi_areaEl=el;return 1;}}
return 0;}
function bmi_resetTitle(el){if(!el)
return;if(el.bmi_touched!=1)
return;el.title="";if(el.bmi_oldTitle){el.title=el.bmi_oldTitle;if(el.bmi_oldAlt){el.alt=el.bmi_oldAlt;}}
else if(el.bmi_oldAlt){el.alt=el.bmi_oldAlt;if(bmi_ie)
el.title=el.alt;}
if(el.bmi_gotOriginal){if(el.bmi_orig_mouseout){el.onmouseout=el.bmi_orig_mouseout;}}}
function bmi_checkElement(el){var pwindow=null;if(el.bmi_gotOriginal)
return;if(el.bmi_mapImage){if(el.bmi_mapImage.bmi_gotOriginal==1){el.bmi_gotOriginal=1;if(el.bmi_touched)
bmi_resetTitle(el);return;}}
if(el.bmi_touched!=1){bmi_setElementTitle(el);if(el.onmouseout){el.bmi_orig_mouseout=el.onmouseout;el.onmouseout=bmi_safeMouseOutEvents;}
else{el.onmouseout=bmi_safeMouseOutEvents;}}
else{el.title=el.bmi_title;el.alt=el.bmi_alt;}
if(el.bmi_mapImage)
bmi_imageObjSelected=el.bmi_mapImage;else
bmi_imageObjSelected=el;if(bmi_ie||bmi_opera)
pwindow=document.parentWindow;else if(bmi_nsonly||is_gecko)
pwindow=document.defaultView;else
pwindow=null;if(pwindow&&(pwindow!=pwindow.parent)){pwindow.focus();el.bmi_changedFocus=1;}
return;}
function bmi_setElementTitle(el){var tmpAlt="";if(el.alt){tmpAlt=el.alt;el.bmi_oldAlt=el.alt;el.bmi_alt="";el.alt="";}
if(el.title){el.bmi_oldTitle=el.title;el.title=el.title+"";}
else{el.title=tmpAlt+"";}
if(bmi_firefox){el.title=el.title+bmi_toolTipSeperator+bmi_ffx_op_toolTip;el.bmi_touched=1;el.bmi_title=el.title;}
else if(bmi_opera||bmi_safari){el.title=bmi_ffx_op_toolTip;el.bmi_touched=1;el.bmi_title=el.title;}
else{el.title=el.title+bmi_toolTipSeperator+bmi_toolTip;el.bmi_touched=1;el.bmi_title=el.title;}
return;}
function bmi_checkInputType(type){if(!type)
return 0;if(type.match("image")||type.match("Image")){return 1;}
return 0;}
function bmi_checkMIMEType(type){var typeStr=new String(type);var find=/image\//gi;if(typeStr.search(find)!=-1)
return 1;return 0;}
function bmi_mouseOver(e){bmi_imageObjSelected=null;var obj;if(document.bmi_onmouseover_original!=null)
document.bmi_onmouseover_original(e);if(bmi_ie||bmi_opera){var e=window.event;obj=e.srcElement;}
else{obj=e.target;}
if(obj.bmi_gotOriginal)
return;if(bmi_ImageElement(obj)){bmi_checkElement(obj);}
return;}
function bmi_safeMouseOutEvents(e){var obj;if(bmi_ie||bmi_opera){e=window.event;obj=e.srcElement;}
else{obj=e.target;}
bmi_resetTitle(obj);if(obj.bmi_changedFocus==1){var pwindow=null;if(bmi_ie||bmi_opera)
pwindow=document.parentWindow;else if(bmi_nsonly||is_gecko)
pwindow=document.defaultView;else
pwindow=null;if(pwindow){pwindow.top.focus();obj.bmi_changedFocus=0;}}
if(obj.bmi_orig_mouseout){obj.bmi_orig_mouseout();}}
function bmi_updateImageSrc(src)
{var found=0;var find=/\?/g;var editUrl;var editIndex;var editProto;var bmiSignIndex;var bmiSign;srcString=new String(src);if(srcString.search(find)!=-1)
{found=1;srcString=srcString.concat("&"+bmi_concatStr+"=1");}
else
{var i=srcString.lastIndexOf("/");var newStr=srcString.substring(i+1);srcString=srcString.concat("/"+bmi_concatStr+"/"+newStr);}//
if(bmi_htmlEdit){editIndex=srcString.indexOf("://");if(editIndex!=-1){editProto=srcString.substring(0,editIndex+3);editUrl=srcString.substring(editIndex+3);editIndex=editUrl.indexOf("/");if(editIndex!=-1){editUrl=editUrl.substring(editIndex+1);bmiSignIndex=editUrl.indexOf("/");if(bmiSignIndex!=-1){bmiSign=editUrl.substring(0,bmiSignIndex);if(bmiSign=="bmi"){editUrl=editUrl.substring(bmiSignIndex+1);srcString=editProto+editUrl;}}}}}
return(srcString);}
function bmi_replaceImages(array){if(!array)
return;for(var i=0;i<array.length;i++){if(array[i].bmi_gotOriginal){continue;}
if(array[i].bmi_objTag){array[i].data=bmi_updateImageSrc(array[i].data);}
else{array[i].src=bmi_updateImageSrc(array[i].src);}
array[i].bmi_gotOriginal=1;if(array[i].bmi_touched){bmi_resetTitle(array[i]);}}
return;}
function bmi_replaceInputImages(array){if(!array)
return;for(var i=0;i<array.length;i++){if(array[i].bmi_gotOriginal){continue;}
if(array[i].type&&bmi_checkInputType(array[i].type)){array[i].src=bmi_updateImageSrc(array[i].src);array[i].bmi_gotOriginal=1;if(array[i].bmi_touched){bmi_resetTitle(array[i]);}}}
return;}
function bmi_NSlayers(){if(document!=null){if(!document.layers){bmi_replaceImages(document.tags.IMG);bmi_replaceInputImages(document.tags.INPUT);return;}
for(var i=0;i<document.layers.length;i++){bmi_NSlayers(document.layers[i].document);bmi_replaceImages(document.layers[i].document.tags.IMG);bmi_replaceInputImages(document.layers[i].document.tags.INPUT);}}
return;}
function bmi_downloadAllHandler(){if((true==bmi_checkAccess(parent))&&(parent.location.href!=self.location.href)){var newparent=parent;do{newparent=newparent.parent;if((false==bmi_checkAccess(newparent.parent))||(newparent.parent.location.href==newparent.location.href)){break;}}while(newparent);var numFrames=newparent.frames.length;var index=0;var frame;for(;index<newparent.frames.length;index++){frame=newparent.frames[index];if(false==bmi_checkAccess(frame.window)){continue;}
if(frame.window.bmi_reDownloadAllImages){frame.window.bmi_reDownloadAllImages();}}
return;}
bmi_reDownloadAllImages();}
function bmi_reDownloadAllImages(){var imgArray;var inputArray;var backgroundArray;var numFrames=window.frames.length;var index=0;var frame;for(;index<numFrames;index++){frame=window.frames[index];if(false==bmi_checkAccess(frame.window)){continue;}
if(frame.window.bmi_reDownloadAllImages){frame.window.bmi_reDownloadAllImages();}}
if((bmi_ie5up||bmi_ns6up||bmi_opera5up||bmi_firefox)){imgArray=document.getElementsByTagName("IMG");inputArray=document.getElementsByTagName("INPUT");bmi_replaceImages(imgArray);bmi_replaceInputImages(inputArray);}
else if(bmi_ns&&(bmi_ns4||bmi_ns3)){var imgArray;var docLayers;docLayers=document.layers;if(docLayers&&docLayers.length){for(var layi=0;layi<0;layi++){imgArray=docLayers[layi].document.images;bmi_replaceImages(imgArray);}}
else{ imgArray=document.images;bmi_replaceImages(imgArray);}}
else{imgArray=document.images;bmi_replaceImages(imgArray);}
var bodyElement=document.getElementsByTagName("body")[0];updateBackgroundImages(bodyElement);updateCssBackgroundImages();return;}
function bmi_reDownloadSelectedImage(img){if(img.bmi_gotOriginal){return;}
if(img&&!img.bmi_gotOriginal){if(img.bmi_objTag){img.data=bmi_updateImageSrc(img.data);}
else{img.src=bmi_updateImageSrc(img.src);}
img.bmi_gotOriginal=1;if(img.bmi_touched){bmi_resetTitle(img);}
if(img.bmi_areaEl&&(img.bmi_areaEl.bmi_touched)){bmi_resetTitle(img.bmi_areaEl);img.bmi_areaEl.bmi_gotOriginal=1;}}
return;}
function bmi_keypress(e)
{var reloadSingle=0;var reloadAll=0;var obj;if(bmi_ns){if(bmi_ns6up){if((String.fromCharCode(e.charCode)=='r')||(String.fromCharCode(e.charCode)=='R'))
reloadSingle=1;else{if((String.fromCharCode(e.charCode)=='A'))
reloadAll=1;}
obj=e.target;var str=new String(obj.tagName);if(str.match("INPUT")&&(bmi_checkInputType(obj.type)!=1)){if(bmi_imageObjSelected==obj)
reloadAll=reloadSingle=0;}}
else{if((String.fromCharCode(e.which)=='R')&&(e.modifiers==Event.SHIFT_MASK))
reloadSingle=1;else{if((String.fromCharCode(e.which)=='A')&&(e.modifiers==Event.SHIFT_MASK))
reloadAll=1;}}}
if(bmi_ie||bmi_opera){if((String.fromCharCode(window.event.keyCode)=='R')&&(window.event.shiftKey))
reloadSingle=1;else if(bmi_opera){if((String.fromCharCode(window.event.keyCode)=='A')&&(window.event.shiftKey))
reloadAll=1;}
var e=window.event;obj=e.srcElement;var str=new String(obj.tagName);if(str.match("INPUT")&&(bmi_checkInputType(obj.type)!=1)){if(bmi_imageObjSelected==obj)
reloadSingle=reloadAll=0;}}
if(reloadSingle==1){if(bmi_ns){if(bmi_ns4||bmi_ns3||bmi_ns2){return;}}
if(bmi_imageObjSelected)
bmi_reDownloadSelectedImage(bmi_imageObjSelected);}
else{if(reloadAll==1){bmi_downloadAllHandler();}}
if((document.bmi_onkeypress_original!=null)&&(document.bmi_onkeypress_original!=bmi_keypress))
{return(document.bmi_onkeypress_original(e));}
return;}
function bmi_linkMapImages(maps,objs){var linked=0;for(var i=0;i<objs.length;i++){if(linked>=maps.length){return linked;}
if(objs[i].useMap){var newStr=new String(objs[i].useMap);var mapName=newStr.substring(newStr.lastIndexOf("")+1);if(bmi_ImageElement(objs[i])!=1)
continue;for(var j=0;j<maps.length;j++){if(maps[j].name==mapName){maps[j].bmi_imgObj=objs[i];linked++;}}}}
return linked;}
function bmi_load(){if(bmi_orig_onLoad){bmi_orig_onLoad();}
if(bmi_ns2||bmi_ns3||bmi_ns4){window.defaultStatus=bmi_ns_tooltip;return;}
if(document.onmouseover){if(document.onmouseover!=bmi_mouseOver){document.bmi_onmouseover_original=document.onmouseover;}}
document.onmouseover=bmi_mouseOver;if(document.onkeypress){if(document.onkeypress!=bmi_keypress){document.bmi_onkeypress_original=document.onkeypress;}}
else{document.bmi_onkeypress_original=null;}
document.onkeypress=bmi_keypress;var maps=document.getElementsByTagName("MAP");if((maps==null)||(maps.length==0)){return;}
var objs=null;if(bmi_ie||bmi_opera){objs=document.all;if(objs){bmi_linkMapImages(maps,objs);}}
if(bmi_ns||is_gecko){var num=0;objs=document.getElementsByTagName("IMG");if(objs){num=num+bmi_linkMapImages(maps,objs);}
if(num>=maps.length){return;}
objs=null;objs=document.getElementsByTagName("INPUT");if(objs){num+=bmi_linkMapImages(maps,objs);}
if(num>=maps.length){return;}
objs=null;objs=document.getElementsByTagName("OBJECT");if(objs){num+=bmi_linkMapImages(maps,objs);}}
return;}
var bmi_orig_onLoad;function bmi_SafeAddOnload(f,urlStr,htmlEdit)
{if(urlStr){bmi_concatStr=urlStr;}
if(htmlEdit){bmi_htmlEdit=htmlEdit;}
if(bmi_ie4){window.onload=f;}
else if(window.onload){if(window.onload!=f){bmi_orig_onLoad=window.onload;window.onload=f;}}
else{window.onload=f;}}
function updateCssBackgroundImages(){var sss=document.styleSheets;var rs;for(var i=0;i<sss.length;i++){if(sss[i].cssRules){updateRuleBackgroundImages(sss[i].cssRules);}
else if(sss[i].rules){updateRuleBackgroundImages(sss[i].rules);}}}
function updateRuleBackgroundImages(rs){for(var i=0;i<rs.length;i++){if(rs[i].style.backgroundImage){var url=geturl(rs[i].style.backgroundImage);url=trimurl(url);var updatedImage=bmi_updateImageSrc(url);rs[i].style.backgroundImage="url("+updatedImage+")";}}}
function updateBackgroundImages(n){var nrTags=0;if(n.nodeType==1){nrTags++;var bgAttr=n.getAttribute("background");if(bgAttr){n.setAttribute("background",bmi_updateImageSrc(bgAttr));}
if(n.style.backgroundImage){var url=geturl(n.style.backgroundImage);url=trimurl(url);var updatedImage=bmi_updateImageSrc(url);n.style.backgroundImage="url("+updatedImage+")";}
var children=n.childNodes;for(var i=0;i<children.length;i++){nrTags+=updateBackgroundImages(children[i]);}}
return nrTags;}
function geturl(bgImage){var str=new String(bgImage);var start=str.indexOf('(');var end=str.indexOf(')');start=(start==-1)?0:start+1;end=(end==-1)?str.length:end;return str.substring(start,end);}
function trimurl(str){var start=0;var end=str.length;for(var i=0;i<str.length;i++){if((str.charAt(i)==' ')||(str.charAt(i)=='"')){start++;}
else{break;}}
var lastOffset=str.length-1;for(var i=0;i<str.length;i++){if((str.charAt(lastOffset-i)==' ')||(str.charAt(lastOffset-i)=='"')){end--;}
else{break;}}
return str.substring(start,end);}
