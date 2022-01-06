$(document).ready(
    function() {
        $("#shortener").submit(
            function(event) {
                event.preventDefault();
                $.ajax({
                    type : "POST",
                    url : "/api/link",
                    data : $(this).serialize(),
                    success : function(msg, status, request) {

                        var response = JSON.parse(request.responseText);

                        if(response.qr) {

                            var qrResponse = $.ajax({url :
                                response.qr,
                                success: function(){

                                    var image = qrResponse.responseJSON.image

                                    $("#result").html(
                                        "<div class='alert alert-success lead'><a target='_blank' href='"
                                        + request.getResponseHeader('Location')
                                        + "'>"
                                        + request.getResponseHeader('Location')
                                        + "</a> "


                                        + "<img width='100' height='100' src='data:image/png;base64, "
                                        + image
                                        + "'>"
                                        + "</div>"
                                        )
                                }});
                        } else {
                            $("#result").html(
                                "<div class='alert alert-success lead'><a target='_blank' href='"
                                + request.getResponseHeader('Location')
                                + "'>"
                                + request.getResponseHeader('Location')
                                + "</a> "
                                + "</div>"
                            )
                        }
                    },
                    error : function() {
                        $("#result").html(
                            "<div class='alert alert-danger lead'>ERROR</div>");
                    }
                });
            });
    });