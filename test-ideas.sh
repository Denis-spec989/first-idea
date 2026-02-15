curl -X POST http://localhost:8080/ideas \
  -H "Content-Type: application/json" \
  -d '{"ideaName": "My first idea", "ideaRate": 5}' \
  -v
