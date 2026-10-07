import express from "express";
import path from "node:path";
import process from "node:process";
import url from "node:url";

const cwd = path.dirname(url.fileURLToPath(import.meta.url));
const top = path.resolve(cwd, "..");
const index = path.resolve(top, "get.html");
const port = process.env?.PORT || 4200;
const server = express();

server.use(express.static(top));

server.get("/", (request, response) => response.sendFile(index));

server.listen(port, () => {
  console.log(`Local server listening on port ${port}, using ${index}.`);
});
