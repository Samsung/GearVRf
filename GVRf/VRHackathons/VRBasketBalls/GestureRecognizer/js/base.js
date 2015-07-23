 // The folloing code is very much a proof of concept. Although functional, there are wholes and bits that have not been fully debuged because of lack of time. Also, the URL for the Berkeley Labs no longer works.
 
  $(document).ready(function() {
    var ctl = new Leap.Controller({enableGestures: true});

    var swiper = ctl.gesture('swipe');

    var tolerance = 20;
    var cooloff = 200;
    var x = 0; var y = 0;

    var xArray = new Array; var yArray = new Array;
    var slider = _.debounce(function(xDir, yDir) {
       // Use the following to tune the motion detector as needed.
      /*
      x += xDir;
      x = (x + 5) % 5;
      y += yDir;
      y = (y + 5) % 5;
      console.log("x:"+x);
      console.log("y:"+y);
      */

      toggleRendering(xDir, yDir, xArray, yArray);
      //updateHighlight();
    }, cooloff);

    swiper.update(function(g) {
      if (Math.abs(g.translation()[0]) > tolerance || Math.abs(g.translation()[1]) > tolerance) {
        var xDir = Math.abs(g.translation()[0]) > tolerance ? (g.translation()[0] > 0 ? -1 : 1) : 0;
        var yDir = Math.abs(g.translation()[1]) > tolerance ? (g.translation()[1] < 0 ? -1 : 1) : 0;
        //console.log('gtrans0: '+g.translation()[0]);
        console.log('gtrans1: '+g.translation()[2]);
        xArray.push(g.translation()[0]);
        yArray.push(g.translation()[1]);
        slider(xDir, yDir);
        
      }
    });

    ctl.connect();
   //updateHighlight();


  })


  function toggleRendering(xDir, yDir, xArray, yArray)
    {

      var textObject = document.getElementsByTagName("Text");
      var text = textObject[0];
      var textVal = '';
      var sum = 0;  var xAve = 0;

      //console.log('xdir: '+xDir);
      //console.log('ydir: '+yDir);

      for( var i = 0; i < xArray.length; i++ ){
          sum += parseInt( xArray[i], 10 ); //don't forget to add the base
      }

      xAvg = sum/xArray.length;
      console.log('xAve: '+xAvg);

      for( var i = 0; i < yArray.length; i++ ){
          sum += parseInt( yArray[i], 10 ); //don't forget to add the base
      }

      yAvg = sum/yArray.length;
      console.log('yAve: '+yAvg);

      if(xDir>0) { textVal='Right'; }
      if(xDir<0) { textVal='Left'; }
      if(yDir>0) { textVal='Down'; }
      if(yDir<0) { textVal='Up';  }
  
      text.setAttribute('string', textVal);

      
      return false;
    }




var xmlhttp;

function loadXMLDoc(url)
{
    xmlhttp=null;
if (window.XMLHttpRequest)
  {// code for all new browsers
      xmlhttp=new XMLHttpRequest();
  }
else if (window.ActiveXObject)
  {// code for IE5 and IE6
      xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }
if (xmlhttp!=null)
  {
      xmlhttp.onreadystatechange=state_Change;
      xmlhttp.open("GET",url,true);
      xmlhttp.send(null);
  }
else
  {
      alert("Your browser does not support XMLHTTP.");
  }
}

function state_Change()
{
    if (xmlhttp.readyState==4)
      {// 4 = "loaded"
          if (xmlhttp.status==200)
            {// 200 = OK
             console.log(xmlhttp);
            // ...our code here...
        }
  else
        {
            
        }
  }
}
