import os

newest_version = 0
newest_bot = ""

for bot in os.listdir('src'):
    if bot[-1].isalpha():
        continue
    
    bot_version = int(bot[-1])

    if bot_version > newest_version:
        newest_version = bot_version
        newest_bot = bot


os.mkdir(f'submission')
os.mkdir(f'submission/{newest_bot}')

for f in os.listdir(f'src/{newest_bot}'):
    if f.endswith('.java'):
        with open(f'src/{newest_bot}/{f}', 'r') as file:
            lines = file.readlines()

        with open(f'submission/{newest_bot}/{f}', 'w') as file:
            for line in lines:
                
                # if line contains rc.setIndicatorString comment out
                if 'rc.setIndicatorString' in line:
                    line = '//' + line
                
                file.write(line)
