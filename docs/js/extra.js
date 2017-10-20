$(function(){

  $('.code-java').replaceWith(function() {
    return $('<div class="code-switcher"> <button class="tab-java" onclick="switchCode(0)">Java</button> <button class="tab-kotlin" onclick="switchCode(1)">Kotlin</button> </div><pre class="code java"><code>' + this.innerHTML + '</code></pre>');
  });

  $('.code-kotlin').replaceWith(function() {
    return $('<pre class="code kotlin"><code>' + this.innerHTML + '</code></pre>');
  });

  switchCode(0);

})

function switchCode(item) {

  // hide all code blocks
  $('.code').hide();

  // show selected code blocks and change selected tab
  switch (item) {
    case 0:
      $('.java').show();
      $('.tab-java').addClass('active')
      $('.tab-kotlin').removeClass('active')
      break;
    case 1:
      $('.kotlin').show();
      $('.tab-kotlin').addClass('active')
      $('.tab-java').removeClass('active')
      break;
    default:
      $('.java').show();
      $('.tab-java').addClass('active')
      $('.tab-kotlin').removeClass('active')
      break;
  }

}