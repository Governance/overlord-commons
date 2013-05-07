var OVERLORD_HEADER_DATA_DEFAULTS = {
  "username" : "uname",
  "logoutLink" : "?GLO=true",
  "tabs" : [
      { "label":"DTGov", "href":"/dtgov", "active":false },
      { "label":"RTGov", "href":"/rtgov", "active":false },
      { "label":"S-RAMP", "href":"/s-ramp-ui", "active":true },
      { "label":"Gadget Server", "href":"/gadget-server", "active":false }
  ]
};

var OVERLORD_HEADER_TEMPLATE = '\
<div class="overlord-navbar">\
  <div class="overlord-navbar-inner">\
    <a class="brand">JBoss - Overlord</a>\
    <div class="overlord-desktop-only">\
      <div class="overlord-navbar-tabs">\
      </div>\
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
<div class="overlord-mobile-nav">\
  <ul class="overlord-nav-list">\
    <li class="overlord-nav-header overlord-mobile-navigation">Navigation</li>\
    <li class="overlord-nav-header overlord-header-username"></li>\
    <li><a class="overlord-nav-logout" href="#">Logout</a></li>\
  </ul>\
</div>\
';

/**
 * Creates the markup needed for a link in the mobile section of the
 * header.
 * @param tab
 * @returns {String}
 */
function ovl_createMobileLinkHtml(tab) {
    var markup = $('<li><a></a></li>');
    if (tab.active) {
        $(markup).addClass('active');
    }
    $(markup).find('a').attr('href', tab.href);
    $(markup).find('a').text(tab.label);
    return markup;
}

/**
 * Creates the markup needed for a tab in the desktop only navigation
 * section of the header.
 * @param tab
 * @param index
 * @param numTabs
 */
function ovl_createNavigationTab(tab, index, numTabs) {
    var markup = $('<div class="overlord-navbar-tab">\
            <div class="left component"></div>\
            <div class="middle component">\
              <a></a>\
            </div>\
            <div class="right component"></div>\
          </div>');
    if (index == 0) {
        $(markup).addClass('overlord-navbar-tab-first');
    }
    if (index == (numTabs-1)) {
        $(markup).addClass('overlord-navbar-tab-last');
    }
    if (tab.active) {
        $(markup).addClass('active');
    }
    $(markup).find('a').attr('href', tab.href);
    $(markup).find('a').text(tab.label);
    return markup;
}

/**
 * Register a function that will render the header when the page loads.  This
 * function expects to find a div with id='overlord-header', which it will use
 * as the container for the header.  If such a div is not present, the header
 * will not be created.
 */
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
    if (data.tabs) {
        var tabs = data.tabs;
        if (tabs.length > 1) {
            for (var i=0; i < data.tabs.length; i++) {
                var tab = data.tabs[i];
                var tabHtml = ovl_createNavigationTab(tab, i, data.tabs.length);
                $('#overlord-header .overlord-navbar .overlord-navbar-tabs').append(tabHtml);
            }
        }
        for (var i = data.tabs.length-1; i >= 0; i--) {
            var tab = data.tabs[i];
            var linkHtml = ovl_createMobileLinkHtml(tab);
            $('#overlord-header .overlord-mobile-nav .overlord-nav-list .overlord-mobile-navigation').after(linkHtml);
        }
    }
    $('#overlord-header .overlord-nav-mobile').click(function() {
        $('#overlord-header .overlord-mobile-nav').slideToggle();
    });
});

