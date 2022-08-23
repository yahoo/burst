$(function () {
  jQuery(document).scroll(function () {
    var $nav = $("#header-burst");
    $nav.toggleClass('scrolled', $(this).scrollTop() > $nav.height());
  });
});