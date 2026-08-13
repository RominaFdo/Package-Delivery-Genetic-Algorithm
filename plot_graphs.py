import csv
import matplotlib.pyplot as plt

# ---------- Load generation stats ----------
generations, best_fit, avg_fit = [], [], []
with open("generation_stats.csv") as f:
    reader = csv.DictReader(f)
    for row in reader:
        generations.append(int(row["generation"]))
        best_fit.append(float(row["bestFitness"]))
        avg_fit.append(float(row["avgFitness"]))

# ---------- Graph 1: Convergence (best fitness only) ----------
plt.figure(figsize=(8, 5))
plt.plot(generations, best_fit, color="tab:blue", linewidth=2)
plt.xlabel("Generation")
plt.ylabel("Best Fitness (Total Route Distance)")
plt.title("Graph 1 - Convergence: Best Fitness over Generations")
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig("graph1_convergence.png", dpi=150)
plt.close()

# ---------- Graph 2: Population behaviour (best + average) ----------
plt.figure(figsize=(8, 5))
plt.plot(generations, best_fit, label="Best Fitness", color="tab:blue", linewidth=2)
plt.plot(generations, avg_fit, label="Average Population Fitness", color="tab:orange", linewidth=2)
plt.xlabel("Generation")
plt.ylabel("Fitness (Total Route Distance)")
plt.title("Graph 2 - Population Behaviour: Best vs Average Fitness")
plt.legend()
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig("graph2_population_behaviour.png", dpi=150)
plt.close()

# ---------- Graph 3: Final route map ----------
locations = {}
with open("locations.csv") as f:
    reader = csv.DictReader(f)
    for row in reader:
        locations[row["id"]] = (float(row["x"]), float(row["y"]))

routes = {"1": [], "2": []}
with open("best_route.csv") as f:
    reader = csv.DictReader(f)
    for row in reader:
        routes[row["vehicle"]].append(int(row["stop_order"]))

plt.figure(figsize=(7, 7))
depot = locations.get("0") or locations.get("depot")
if depot is None:
    raise KeyError("Missing depot location: expected row with id '0' or 'depot'")
depot_x, depot_y = depot
plt.scatter([depot_x], [depot_y], color="black", marker="s", s=120, label="Depot", zorder=5)

# label every location point
for loc_id, (x, y) in locations.items():
    if loc_id in {"0", "depot"}:
        continue
    plt.scatter([x], [y], color="gray", s=40, zorder=3)
    plt.annotate(loc_id, (x, y), textcoords="offset points", xytext=(5, 5), fontsize=9)

colors = {"1": "tab:red", "2": "tab:green"}
with open("best_route.csv") as f:
    reader = csv.DictReader(f)
    stops = {"1": [], "2": []}
    for row in reader:
        stops[row["vehicle"]].append(int(row["location_id"]))

for vehicle, loc_ids in stops.items():
    path_x = [depot_x] + [locations[str(l)][0] for l in loc_ids] + [depot_x]
    path_y = [depot_y] + [locations[str(l)][1] for l in loc_ids] + [depot_y]
    plt.plot(path_x, path_y, color=colors[vehicle], marker="o",
              label=f"Vehicle {vehicle} route", linewidth=2, alpha=0.8)

plt.xlabel("X (km)")
plt.ylabel("Y (km)")
plt.title("Graph 3 - Best Delivery Route Found")
plt.legend()
plt.grid(True, alpha=0.3)
plt.axis("equal")
plt.tight_layout()
plt.savefig("graph3_route_map.png", dpi=150)
plt.close()

print("Saved: graph1_convergence.png, graph2_population_behaviour.png, graph3_route_map.png")