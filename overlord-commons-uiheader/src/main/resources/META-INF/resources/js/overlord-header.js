var OVERLORD_HEADER_DATA_DEFAULTS = {
  "primaryBrand" : "JBoss Overlord",
  "secondaryBrand" : "S-RAMP Repository",
  "username" : "jdoe",
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
    <div class="overlord-navbar-brand">\
      <a class="brand"></a>\
      <a class="subbrand overlord-desktop-only"></a>\
      <div class="overlord-nav-user overlord-desktop-only">\
        <a href="#">\
          <span class="overlord-nav-username overlord-header-username"></span>\
        </a>\
      </div>\
      <div class="overlord-nav-user-menu">\
        <ul>\
          <li><a class="overlord-nav-logout">Logout</a></li>\
        </ul>\
      </div>\
      <div class="overlord-nav-mobile overlord-mobile-only">\
        Menu\
      </div>\
    </div>\
    <div class="overlord-navbar-nav overlord-desktop-only">\
        <div class="overlord-navbar-tabs">\
        </div>\
    </div>\
  </div>\
  <div class="overlord-mobile-nav">\
    <ul class="overlord-nav-list overlord-mobile-only">\
      <li class="overlord-nav-header overlord-mobile-navigation">Navigation</li>\
      <li class="overlord-nav-header overlord-header-username"></li>\
      <li>\
        <a class="overlord-nav-logout">Logout</a>\
      </li>\
    </ul>\
  </div>';

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
function ovl_createNavigationTab(tab) {
    var markup = $('<a class="overlord-navbar-tab"></a>');
    if (tab.active) {
        $(markup).addClass('active');
    }
    $(markup).attr('href', tab.href);
    $(markup).text(tab.label);
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
    $('#overlord-header a.brand').text(data.primaryBrand);
    $('#overlord-header a.subbrand').text(data.secondaryBrand);
    $('#overlord-header .overlord-header-username').text(data.username);
    $('#overlord-header a.overlord-nav-logout').attr("href", data.logoutLink);
    if (data.tabs) {
        var tabs = data.tabs;
        if (tabs.length > 0) {
            for (var i=0; i < data.tabs.length; i++) {
                var tab = data.tabs[i];
                var tabHtml = ovl_createNavigationTab(tab);
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
        $('#overlord-header .overlord-mobile-nav').slideToggle('fast');
    });
    $('#overlord-header .overlord-nav-user').click(function(event) {
        event.preventDefault();
        event.stopPropagation();
        var h = $(this).position().top + $(this).outerHeight();
        var w = $(this).outerWidth() - 1;
        $('#overlord-header .overlord-nav-user-menu').css({
            'right': 0,
            'top': h,
            'min-width': w
        });
        $('#overlord-header .overlord-nav-user-menu').slideToggle('fast');
    });
    $('body').click(function() {
        $('#overlord-header .overlord-nav-user-menu').hide();
    });
});

