const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/pigball'
});

const movementState = {
    up: false,
    down: false,
    left: false,
    right: false
  };

// Listen for keydown events to update movementState
document.addEventListener("keydown", (e) => {
    switch (e.key) {
        case "ArrowUp":
            movementState.up = true;
            break;
        case "ArrowDown":
            movementState.down = true;
            break;
        case "ArrowLeft":
            movementState.left = true;
            break;
        case "ArrowRight":
            movementState.right = true;
            break;
        default:
            break;
    }
});

// Listen for keyup events to update movementState
document.addEventListener("keyup", (e) => {
    switch (e.key) {
        case "ArrowUp":
            movementState.up = false;
            break;
        case "ArrowDown":
            movementState.down = false;
            break;
        case "ArrowLeft":
            movementState.left = false;
            break;
        case "ArrowRight":
            movementState.right = false;
            break;
        default:
            break;
    }
});


function connect() {
    const FRAME_RATE = 60;
    let playerName = $("#my_name").val(); // Get the player's name from input

    if (!playerName) {
        alert("Please enter your name before connecting!");
        return;
    }

    stompClient.onConnect = (frame) => {
        setConnected(true);
        console.log('Connected: ' + frame);

        // Subscribe to game updates
        stompClient.subscribe('/topic/play', (board) => {
            showBoard(JSON.parse(board.body));
        });

        stompClient.subscribe('/topic/players', (message) => {
            let players = JSON.parse(message.body); // Obtenemos la lista completa de jugadores
            console.log("Updated player list:", players);
        
            // Eliminar todos los jugadores actuales
            $("#players").empty();
        
            // Redibujar los jugadores con la información actualizada
            players.forEach(player => {
                let playerId = `${player.name}_player`;
        
                // Agregar el jugador al contenedor
                $("#players").append(`<div id="${playerId}" class="player"></div>`);
                let playerElement = $(`#${playerId}`);
        
                // Aplicar estilos para posicionamiento
                playerElement.css({
                    "position": "absolute",
                    "top": player.y + "px",
                    "left": player.x + "px"
                });
            });
        });
        
        // Send the player's name to the backend after connecting
        stompClient.publish({
            destination: "/app/join",
            body: JSON.stringify({ "name": playerName })
        });

        // Start sending the movement state every 50ms
        const intervalId = setInterval(() => {
            if (stompClient && stompClient.active) {
                stompClient.publish({
                    destination: "/app/play",
                    body: JSON.stringify({
                        player: playerName,
                        'dx': movementState.right - movementState.left,
                        'dy': movementState.down - movementState.up 
                    })
                });
            } else {
                clearInterval(intervalId); // Detener el intervalo si stompClient no está activo
                console.warn("Interval stopped: Stomp client is inactive.");
            }
        }, 1000 / FRAME_RATE);
    };

    stompClient.activate();
}

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
    console.log("Entering in the set connected section.");
    $("#connect").prop("disabled", connected);
    $("#my_name").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);

    if (connected) {
        $("#board").show();
    } else {
        $("#board").hide();
    }
}


function disconnect() {
    stompClient.publish({
        destination: "/app/leave",
        body: JSON.stringify({ "name": $("#my_name").val() })
    });
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}



function showBoard(gameData) {
    gameData.players.forEach(player => {
        let playerElement = $("#" + player.name+"_player");

        if (playerElement.length) { // Ensure the element exists
            playerElement.css({
                "position": "absolute", // Ensure positioning works
                "top": player.y + "px", // y corresponds to top
                "left": player.x + "px" // x corresponds to left
            });
        } else {
            console.warn(`Player element with ID '${player.name}' not found!`);
        }
    });

}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $("#connect").click(() => connect());
    $("#disconnect").click(() => disconnect());
});

