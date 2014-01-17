  $(document).ready(function(){

    $("#update_record_update_id").change();

    var typ = null;

    $("#update_record_update_id").change(function(){
      if (typ != null) {
        typ.abort();
        typ = null;
      }
      var val = $(this).val();
      $("#loading-img").show();
      $("#update_record_machine_id").prop('disabled', true);
      typ = $.get("/updates/" + val + ".json", function(data) {
        $('#update_record_machine_id').empty();
        $("#update_record_machine_id").prop('disabled', false);
        var machines = data.machines;
        for (var i = 0; i < machines.length; i++) {
          $("#update_record_machine_id").append(new Option(machines[i].name, machines[i].id));
        }
        $("#loading-img").hide();
      }, "json");
    });

  });