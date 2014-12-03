
## Makefile handle something something.



.PHONY: default
default: help

.PHONY: help
help:
	@echo "=================== Available commands ===================="
	@echo "all          - compile -> test -> doc -> typecheck."
	@echo "clean        - clean the project."
	@echo "=========================================================="
	@echo "help         - Show this output."

.PHONY: clean
clean:
	mvn clean

