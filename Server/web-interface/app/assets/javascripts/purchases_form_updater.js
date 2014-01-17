  $(document).ready(function(){

    $("#purchase_typ").change();

    var typ = null;
    var rep = null;

    $("#purchase_typ").change(function(){
      if (typ != null) {
        typ.abort();
        typ = null;
      }
      var val = $(this).val();
      $("#loading-img").show();
      $("#purchase_typ_id").prop('disabled', true);
      var t = [ "group", "school", "machine" ];
      typ = $.get("/" + t[val] + "s.json", function(data) {
        $('#purchase_typ_id').empty();
        $("#purchase_typ_id").prop('disabled', false);
        for (var i = 0; i < data.length; i++) {
          var text = "";
          if (val == 0) {
            text = data[i]['name'];
          }
          else if (val == 1) {
            text = data[i]['name'] + "(" + data[i].group['name'] + ")";
          }
          else {
            text = data[i].machine_id + "(" + data[i]['name'] + ")/" + data[i].school['name'] + "/" + data[i].group['name'];
          }
         $("#purchase_typ_id").append(new Option(text, data[i].id));

        }
        $("#loading-img").hide();
      }, "json");
    });

  });