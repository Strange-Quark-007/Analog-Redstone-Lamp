# Analog Redstone Lamp

Why should redstone lamps only live in a binary world — on or off?  
This mod lets them **glow with nuance**, scaling their **brightness smoothly** with the **redstone signal strength**
they
receive.

A signal of 1 gives a faint pulse, 15 burns bright — finally, your lamps understand subtlety.

---

## Requirements

- Fabric API (only for Fabric Loader)

---

## Technical Note

Because analog updates aren’t natively handled by vanilla’s redstone system, <br/>
the lamp schedules a 1-tick delayed update to properly synchronize its level with changing power levels. <br/>
This keeps visuals and behavior consistent — at the cost of a barely noticeable tick of patience.

---

## License

Licensed under the [MIT License](LICENSE).
