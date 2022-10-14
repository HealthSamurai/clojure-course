up:
	docker-compose up -d db

down:
	docker-compose down

repl:
	clj -M:ui:course:nrepl:ui
