var smartgarden = {
    historyId: "",
    irrigationStatusInterval: -1,
    onDocumentReady: function () {
        this.initIrrigationToggle();
        this.getSoilStatusHistory();
        this.getIrrigationHistory();
        this.getPowerStatusHistory();
    },
    initIrrigationToggle: function () {
        var self = this;
        $.get("/smartgarden/web?action=get_garden_status", function (data) {
            if (data.success) {
                if (data.value.irrigationStatus.waterValveOpen) {
                    $('#irrigation_toggle').bootstrapToggle('on');
                } else {
                    $('#irrigation_toggle').bootstrapToggle('off');
                }
                self.displayGardenStatus(data);
            } else {
                $("#irrigation_message").text(data.errorMessage);
            }
            $('#irrigation_toggle').change(function () {
                var startIrrigation = $(this).prop('checked');
                if (startIrrigation) {
                    self.startIrrigation();
                } else {
                    self.stopIrrigation();
                }
            });
        });
    },

    getGardenStatus: function () {
        var self = this;
        $.get("/smartgarden/web?action=get_garden_status", function (data) {
            if (data.success) {
                if (!data.value.irrigationStatus.waterValveOpen) {
                    if (self.irrigationStatusInterval >= 0) {
                        clearInterval(self.irrigationStatusInterval);
                        self.irrigationStatusInterval = -1;
                    }
                    $('#irrigation_toggle').bootstrapToggle('off');
                }
                self.displayGardenStatus(data);
            } else {
                $("#soil_temperature").text("Error");
                $("#soil_dryLevel").text("Error");
            }
        });
    },
    displayGardenStatus: function (data) {
        $("#water_valve_open").text(data.value.irrigationStatus.waterValveOpen);
        $("#soil_temperature").text(data.value.irrigationStatus.soilStatus.dryLevelAndTemperature.temperatureInCelsius + 'C');
        $("#soil_dryLevel").text(data.value.irrigationStatus.soilStatus.dryLevelAndTemperature.dryLevel);
        $("#waterflow").text(data.value.irrigationStatus.waterflow.milliliter + 'ml');
        $("#battery_voltage").text(data.value.powerStatus.voltageAndCurrent.voltage + 'V');
        $("#battery_current").text(data.value.powerStatus.voltageAndCurrent.currentInMA + 'mA');
        $("#max_drylevel_allowed").text(data.value.config['soil.maxDryLevelAllowed']);
        $("#drylevel_poll_count").text(data.value.config['soil.dryLevelPollCount']);
        $("#drylevel_poll_frequency").text(data.value.config['soil.dryLevelPollFrequencyInMS']);
        $("#irrigation_duration").text(data.value.config['irrigation.durationInSeconds'] + ' seconds');
        var powerLevelClass = "progress-bar progress-bar-success";
        if (data.value.powerStatus.batteryLevelInPercent < 25) {
            powerLevelClass = "progress-bar progress-bar-danger";
        }
        $('#battery_level').attr("class", powerLevelClass);
        $('#battery_level').css('width', data.value.powerStatus.batteryLevelInPercent + '%').attr('aria-valuenow', data.value.powerStatus.batteryLevelInPercent);
        $('#battery_level').text(data.value.powerStatus.batteryLevelInPercent + '%');
    },
    startIrrigation: function () {
        var self = this;
        $("#irrigation_message").text("Irrigation in progress...");
        $.get("/smartgarden/web?action=start_irrigation", function (data) {
            if (data.success) {
                if (data.value != -1) {
                    self.historyId = data.value;
                }
                self.irrigationStatusInterval = setInterval(function () {
                    self.getGardenStatus();
                }, 1500);
            } else {
                $("#irrigation_message").text(data.errorMessage);
            }
        });
    },
    stopIrrigation: function () {
        var self = this;
        $("#irrigation_message").text("Stopping...");
        $.get("/smartgarden/web?action=stop_irrigation", {
            historyId: self.historyId
        }, function (data) {
            if (data.success) {
                $("#irrigation_message").text("Stopped Successfully");
                historyId = "";
                if (self.irrigationStatusInterval >= 0) {
                    clearInterval(self.irrigationStatusInterval);
                    self.irrigationStatusInterval = -1;
                }
                self.getIrrigationHistory();
            } else {
                $("#irrigation_message").text(data.errorMessage);
            }
        });
    },
    getIrrigationHistory: function () {
        $("#irrigation_history > tbody:last").children().remove();
        $.get("/smartgarden/web?action=get_irrigation_history", function (data) {
            if (data.success) {

                for (var i = 0; i < data.value.length; ++i) {
                    var history = data.value[i];
                    $('#irrigation_history > tbody:last-child').append(
                            "<tr><td><a href='/smartgarden/web?action=get_image&filename=" + history.imageFilename + "' target='_blank'><img src='images/photo.png'/></a></td>" +
                            "<td>" + history.startTime + "</td>" +
                            "<td>" + history.endTime + "</td>" +
                            "<td>" + history.waterVolumeInML + "</td></tr>"
                            );
                }
            } else {
                $("#irrigation_message").text(data.errorMessage);
            }

        });
    },
    getPowerStatusHistory: function () {
        $.get("/smartgarden/web?action=get_power_status_history", function (data) {
            if (data.success) {
                var chartDataList = data.value;
                for (var i = 0; i < chartDataList.length; ++i) {
                    var chartData = chartDataList[i];
                    chartData.voltage = chartData.voltageAndCurrent.voltage;                   
                }
                Morris.Line({
                    // ID of the element in which to draw the chart.
                    element: 'solar_power_trend',
                    // Chart data records -- each entry in this array corresponds to a point on
                    // the chart.
                    data: chartDataList,
                    // The name of the data record attribute that contains x-visitss.
                    xkey: 'datetime',
                    // A list of names of data record attributes that contain y-visitss.
                    ykeys: ['voltage'],
                    // Labels for the ykeys -- will be displayed when you hover over the
                    // chart.
                    labels: ['Voltage'],
                    // Disables line smoothing
                    smooth: true,
                    resize: true,
                    ymax:15,
                    ymin:10
                });

            } else {
                $("#irrigation_message").text(data.errorMessage);
            }

        });
    },
    getSoilStatusHistory: function () {
        $.get("/smartgarden/web?action=get_soil_status_history", function (data) {
            if (data.success) {
                var chartDataList = data.value;
                var minDryLevel=0;
                var maxDryLevel=0
                for (var i = 0; i < chartDataList.length; ++i) {
                    var chartData = chartDataList[i];
                    chartData.dryLevel = chartData.dryLevelAndTemperature.dryLevel;
                    chartData.temperature = chartData.dryLevelAndTemperature.temperatureInCelsius;
                    if (i==0){
                        minDryLevel=maxDryLevel=chartData.dryLevel;
                    }else{
                        if (chartData.dryLevel < minDryLevel){
                            minDryLevel=chartData.dryLevel;
                        }
                        if (chartData.dryLevel > maxDryLevel){
                            maxDryLevel=chartData.dryLevel;
                        }
                    }

                }
                Morris.Line({
                    // ID of the element in which to draw the chart.
                    element: 'average_drylevel_trend',
                    // Chart data records -- each entry in this array corresponds to a point on
                    // the chart.
                    data: chartDataList,
                    // The name of the data record attribute that contains x-visitss.
                    xkey: 'datetime',
                    // A list of names of data record attributes that contain y-visitss.
                    ykeys: ['dryLevel'],
                    // Labels for the ykeys -- will be displayed when you hover over the
                    // chart.
                    labels: ['Dry Level'],
                    // Disables line smoothing
                    smooth: true,
                    resize: true,
                    ymax:maxDryLevel,
                    ymin:minDryLevel
                });

            } else {
                $("#irrigation_message").text(data.errorMessage);
            }

        });

    }
};


$(document).ready(function () {
    smartgarden.onDocumentReady();
});
