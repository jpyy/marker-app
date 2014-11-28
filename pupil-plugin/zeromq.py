import zmq

ctx = zmq.Context()
socket = ctx.socket(zmq.SUB)
socket.setsockopt(zmq.SUBSCRIBE, "")
socket.connect('tcp://127.0.0.1:5000')

while True:
    print socket.recv()
