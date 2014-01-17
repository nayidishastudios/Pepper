  $(document).ready(function(){

    $("#update_typ").change();
    $("#update_repository_id").change();

    var typ = null;
    var rep = null;

    $("#update_typ").change(function(){
      if (typ != null) {
        typ.abort();
        typ = null;
      }
      var val = $(this).val();
      $("#loading-img").show();
      $("#update_typ_id").prop('disabled', true);
      var t = [ "group", "school", "machine" ];
      typ = $.get("/" + t[val] + "s.json", function(data) {
        $('#update_typ_id').empty();
        $("#update_typ_id").prop('disabled', false);
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
         $("#update_typ_id").append(new Option(text, data[i].id));

        }
        $("#loading-img").hide();
      }, "json");
    });

    $("#update_repository_id").change(function(){
      if (rep != null) {
        rep.abort();
        rep = null;
      }
      var val = $(this).val();
      $("#loading-img").show();
      $("#update_comit_id").prop('disabled', true);
      rep = $.get("/repositories/" + val + ".json", function(data) {
        $('#update_comit_id').empty();
        $("#update_comit_id").prop('disabled', false);
        var comits = data.comits;
        if (comits != null) {
          for (var i = 0; i < comits.length; i++) {
            if (comits[i].is_update) {
              var date = new Date(comits[i].time);
              $("#update_comit_id").append(new Option(comits[i].comit_hash + ' (' + date.toUTCString() + ')', comits[i].id));
            }
          }
        }
        $("#loading-img").hide();
      }, "json");
    });

  });