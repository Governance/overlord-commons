var OVERLORD_HEADER_DATA_DEFAULTS = {
  "username" : "uname",
  "logoutLink" : "?GLO=true",
  "tabs" : [
	  { "label":"S-RAMP", "href":"/s-ramp-ui", "active":true },
	  { "label":"DTGov", "href":"/dtgov", "active":false },
	  { "label":"RTGov", "href":"/rtgov", "active":false }
  ]
};

var OVERLORD_HEADER_TEMPLATE = '\
<div class="overlord-navbar">\
  <div class="overlord-navbar-inner">\
    <div class="">\
      <a class="brand">JBoss - Overlord</a>\
      <div class="overlord-desktop-only">\
        <div class="overlord-nav-shadowman"></div>\
        <div class="overlord-nav-user">\
          <span class="overlord-nav-username overlord-header-username"></span>\
          <span> &raquo; </span>\
          <a href="#" class="overlord-nav-logout">logout</a>\
        </div>\
      </div>\
      <div class="overlord-mobile-only overlord-nav-mobile">\
        <a class="">Menu</a>\
      </div>\
    </div>\
  </div>\
</div>\
<div class="overlord-mobile-nav">\
  <ul class="overlord-nav-list">\
    <li class="overlord-nav-header overlord-mobile-navigation">Navigation</li>\
    <li class="overlord-nav-header overlord-header-username"></li>\
    <li><a class="overlord-nav-logout" href="#">Logout</a></li>\
  </ul>\
</div>\
';
$(document).ready(function() {
	var data = OVERLORD_HEADER_DATA_DEFAULTS;
	try {
		data = OVERLORD_HEADER_DATA;
	} catch (e) {
		// drop
	}
    $('#overlord-header').html(OVERLORD_HEADER_TEMPLATE);
    $('#overlord-header .overlord-header-username').text(data.username);
    $('#overlord-header a.overlord-nav-logout').attr("href", data.logoutLink);
    var tabs = data.tabs;
    for (var i = data.tabs.length-1; i >= 0; i--) {
    	var tab = data.tabs[i];
    	var tabClass = "";
    	if (tab.active) {
    		tabClass = "active";
    	}
    	var linkHtml = '<li class="'+tabClass+'"><a href="'+tab.href+'">'+tab.label+'</a></li>';
    	$('#overlord-header .overlord-mobile-nav .overlord-nav-list .overlord-mobile-navigation').after(linkHtml);
    }
    $('#overlord-header .overlord-nav-mobile').click(function() {
        $('#overlord-header .overlord-mobile-nav').slideToggle();
    });
});

