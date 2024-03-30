CURSOR_UP = "\033[1A"
CLEAR = "\x1b[2K"
print(1)
print(2)
print(31, end="")
print('\r', end="")
# clears TWO lines
print((CURSOR_UP + CLEAR)*2, end="")
print(4)