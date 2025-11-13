# Cascadia — Digital Board Game

**University Project**  
Developed by an eight-member team as part of a software engineering course.  
This project is a digital adaptation of the award-winning board game *Cascadia*, implemented in Kotlin with a focus on modularity, game logic, and user interface design.

---

## Overview

In *Cascadia*, players build interconnected habitats and populate them with wildlife native to the Pacific Northwest.  
Each round, players choose habitat tiles and wildlife tokens, placing them to create the most harmonious and diverse ecosystem.  
Scoring is based on five animal types, each with its own unique pattern requirements.  
The player with the highest score at the end wins.

---

## Features

- Five animal species with unique selectable scoring rules  
- Habitat tile placement and rotation mechanics  
- Turn-based multiplayer  
- Hotseat, hosting, and lobby joining modes  
- Nature Tokens enabling special actions (tile/token replacement or free selection)  
- Automated scoring and result breakdown  
- Interactive UI built in Kotlin

---

## Screenshots

### Main Menu  
<img width="1751" height="983" alt="Screenshot 2025-11-07 112420" src="https://github.com/user-attachments/assets/4e3ef5d5-588b-4787-90a5-11a9a89eb5e3" />


### Player Setup  
<img width="1750" height="982" alt="Screenshot 2025-11-07 112458" src="https://github.com/user-attachments/assets/9e11cb3f-3848-4ae5-8320-6532436de2d5" />


### Lobby Join  
<img width="1749" height="984" alt="Screenshot 2025-11-07 113259" src="https://github.com/user-attachments/assets/35196d4f-3198-4473-802f-bd2140db73ba" />


### Scoring Card Selection  
<img width="1751" height="982" alt="Screenshot 2025-11-07 112519" src="https://github.com/user-attachments/assets/6146ecb5-7a44-4a48-929d-6013d9547980" />


### Gameplay (Early Board State)  
<img width="1753" height="980" alt="Screenshot 2025-11-07 112536" src="https://github.com/user-attachments/assets/2db53b10-c053-4ccd-b481-9d6c23e650e0" />


### Gameplay (Expanded Board)  
<img width="1750" height="981" alt="Screenshot 2025-11-07 113126" src="https://github.com/user-attachments/assets/ac79786f-5419-476c-a5d4-59225dcb290b" />


### Game Over Screen  
<img width="1749" height="981" alt="Screenshot 2025-11-07 113148" src="https://github.com/user-attachments/assets/b9ced127-8b5b-4650-8505-374e10436d8a" />


### Detailed Score Breakdown  
<img width="1755" height="984" alt="Screenshot 2025-11-07 113233" src="https://github.com/user-attachments/assets/eda9cb3a-9672-4757-9714-8f32b846260d" />


---

## Gameplay Summary

1. Enter your name(s) and select the player types  
2. Create or join a lobby  
3. Choose wildlife scoring cards  
4. On your turn:  
   - Select a habitat tile  
   - Place the tile in your map  
   - Place the associated wildlife token  
   - Optionally use a Nature Token  
5. End the turn and pass to the next player  

After the final tile is placed, scoring is calculated automatically.  
A detailed guide is available in **HowToPlay.pdf**.

---

## Development Details

- **Language:** Kotlin  
- **Build Tool:** Gradle  
- **IDE:** IntelliJ IDEA
- **Version Control:** GitLab
- **Team Size:** 8 developers

The development process focused on modular architecture, clean separation of game logic and UI components, and collaborative version control using GitLab.

---

## Credits

Inspired by the original *Cascadia* board game designed by Randy Flynn.  
All rights to the original board game belong to Flatout Games.

---

© 2025 Jason Ball.  
This project was created for educational and portfolio purposes.  
Commercial use or redistribution of included code and assets is not permitted.
